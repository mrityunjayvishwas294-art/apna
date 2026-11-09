import { test, describe, before, after } from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails,
} from '@firebase/rules-unit-testing';
import {
  doc,
  getDoc,
  setDoc,
  updateDoc,
  deleteDoc,
  collection,
  getDocs,
  query,
  where,
  serverTimestamp,
} from 'firebase/firestore';

const PROJECT_ID = 'gen-lang-client-0398695106';
const ADMIN_UID = 'admin-uid-1';
const ADMIN_EMAIL = 'mrityunjayvishwas294@gmail.com';
const USER_A = 'customer-uid-a';
const USER_B = 'customer-uid-b';

let testEnv;

function dbFor(uid, extraToken = {}) {
  if (!uid) return testEnv.unauthenticatedContext().firestore();
  return testEnv.authenticatedContext(uid, extraToken).firestore();
}

function adminDb() {
  return dbFor(ADMIN_UID, { email: ADMIN_EMAIL, email_verified: true });
}

describe('Apna Market Firestore Security Rules', () => {
  before(async () => {
    const rules = fs.readFileSync('firestore.rules', 'utf8');
    testEnv = await initializeTestEnvironment({
      projectId: PROJECT_ID,
      firestore: {
        rules,
        host: '127.0.0.1',
        port: 8085,
      },
    });
  });

  after(async () => {
    await testEnv.cleanup();
  });

  test('unauthenticated users cannot read or write any collection', async () => {
    const unauth = dbFor(null);
    await assertFails(getDoc(doc(unauth, 'products/prod_1')));
    await assertFails(getDoc(doc(unauth, 'store_config/main')));
    await assertFails(getDoc(doc(unauth, 'orders/AM-1001')));
    await assertFails(setDoc(doc(unauth, 'orders/AM-1001'), { id: 'AM-1001' }));
  });

  test('admin can write store_config, products, banners; customers can read but not write', async () => {
    const admin = adminDb();
    const customer = dbFor(USER_A, { email: 'custA@example.com', email_verified: true });

    await assertSucceeds(setDoc(doc(admin, 'store_config/main'), {
      shopName: 'अपना मार्केट',
      deliveryFee: 60,
      isAcceptingOrders: true,
    }));

    await assertSucceeds(setDoc(doc(admin, 'products/veg_1'), {
      id: 'veg_1',
      name: 'Fresh Potato',
      hindiName: 'ताज़ा आलू',
      category: 'VEGETABLES',
      unit: '1 kg',
      mrp: 40,
      ourPrice: 30,
      inStock: true,
      variants: [{ label: '500 gm', mrp: 20, ourPrice: 15 }],
    }));

    await assertSucceeds(setDoc(doc(admin, 'banners/ban_1'), {
      id: 'ban_1',
      title: 'Festival Sale',
      subtitle: 'Flat 20% off',
      isActive: true,
    }));

    // Customer can read
    await assertSucceeds(getDoc(doc(customer, 'store_config/main')));
    await assertSucceeds(getDoc(doc(customer, 'products/veg_1')));
    await assertSucceeds(getDoc(doc(customer, 'banners/ban_1')));

    // Customer cannot write or delete
    await assertFails(setDoc(doc(customer, 'products/veg_2'), {
      id: 'veg_2',
      name: 'Hack',
      category: 'VEGETABLES',
      unit: '1 kg',
      mrp: 10,
      ourPrice: 5,
      inStock: true,
    }));
    await assertFails(deleteDoc(doc(customer, 'products/veg_1')));
    await assertFails(updateDoc(doc(customer, 'store_config/main'), { deliveryFee: 0 }));
  });

  test('unverified admin email cannot act as admin', async () => {
    const fakeAdmin = dbFor('fake-uid', { email: ADMIN_EMAIL, email_verified: false });
    await assertFails(setDoc(doc(fakeAdmin, 'products/veg_fake'), {
      id: 'veg_fake',
      name: 'Fake',
      category: 'VEGETABLES',
      unit: '1 kg',
      mrp: 10,
      ourPrice: 5,
      inStock: true,
    }));
  });

  test('orders are strictly isolated to owner and admin; customer can only rate/tip their own order', async () => {
    const custA = dbFor(USER_A);
    const custB = dbFor(USER_B);
    const admin = adminDb();

    const orderData = {
      id: 'AM-1001',
      userId: USER_A,
      customerName: 'Customer A',
      customerPhone: '9999999999',
      area: 'पुंजीपथरा',
      addressDetail: 'Near Chowk',
      items: [{ productId: 'veg_1', name: 'Potato', unit: '1 kg', price: 30, quantity: 1 }],
      subtotal: 30,
      deliveryCharge: 60,
      grandTotal: 90,
      status: 'RECEIVED',
      createdAt: serverTimestamp(),
    };

    // Customer A cannot forge an order for Customer B
    await assertFails(setDoc(doc(custA, 'orders/AM-9999'), { ...orderData, id: 'AM-9999', userId: USER_B }));

    // Customer A creates their own order
    await assertSucceeds(setDoc(doc(custA, 'orders/AM-1001'), orderData));

    // Customer A can read own order; Customer B cannot read Customer A's order
    await assertSucceeds(getDoc(doc(custA, 'orders/AM-1001')));
    await assertFails(getDoc(doc(custB, 'orders/AM-1001')));

    // Customer A can query their own orders; Customer B cannot list all orders
    await assertSucceeds(getDocs(query(collection(custA, 'orders'), where('userId', '==', USER_A))));
    await assertFails(getDocs(collection(custB, 'orders')));

    // Customer A can rate/tip their own order, but cannot change status or grandTotal
    await assertSucceeds(updateDoc(doc(custA, 'orders/AM-1001'), {
      rating: 5,
      ratingReview: 'Great!',
      deliveryTip: 20,
    }));
    await assertFails(updateDoc(doc(custA, 'orders/AM-1001'), { status: 'DELIVERED' }));
    await assertFails(updateDoc(doc(custA, 'orders/AM-1001'), { grandTotal: 1 }));

    // Admin can read all orders and update status
    await assertSucceeds(getDocs(collection(admin, 'orders')));
    await assertSucceeds(updateDoc(doc(admin, 'orders/AM-1001'), { status: 'PACKED' }));
  });

  test('custom_requests are isolated to owner and admin', async () => {
    const custA = dbFor(USER_A);
    const custB = dbFor(USER_B);
    const admin = adminDb();

    await assertSucceeds(setDoc(doc(custA, 'custom_requests/REQ-101'), {
      id: 'REQ-101',
      userId: USER_A,
      customerName: 'Customer A',
      customerPhone: '9999999999',
      itemName: 'Special Sweets',
      quantity: '1 kg',
      status: 'Reviewing',
      createdAt: serverTimestamp(),
    }));

    await assertSucceeds(getDoc(doc(custA, 'custom_requests/REQ-101')));
    await assertFails(getDoc(doc(custB, 'custom_requests/REQ-101')));
    await assertFails(updateDoc(doc(custA, 'custom_requests/REQ-101'), { status: 'Done' }));

    await assertSucceeds(updateDoc(doc(admin, 'custom_requests/REQ-101'), { status: 'Brought from Raigarh' }));
  });
});
