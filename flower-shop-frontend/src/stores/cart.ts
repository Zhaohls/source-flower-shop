import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import request from '@/api/request';

type CartItemResponse = {
  id: number;
  productId: number;
  productName: string;
  merchantId?: number;
  merchantName?: string;
  mainImage: string;
  price: number;
  quantity: number;
  selected: number;
  stock: number;
  invalid: boolean;
  invalidReason?: string;
  supportCustomize: boolean;
};

export interface CartItem {
  id: string;
  productId: number;
  name: string;
  price: number;
  quantity: number;
  image: string;
  merchantId?: number;
  merchantName?: string;
  checked: boolean;
  stock: number;
  invalid: boolean;
  invalidReason?: string;
  supportCustomize: boolean;
}

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>([]);
  const loading = ref(false);

  const validItems = computed(() => items.value.filter(item => !item.invalid));
  const totalCount = computed(() => validItems.value.reduce((total, item) => total + item.quantity, 0));
  const selectedCount = computed(() => validItems.value
    .filter(item => item.checked)
    .reduce((total, item) => total + item.quantity, 0));
  const totalPrice = computed(() => validItems.value
    .filter(item => item.checked)
    .reduce((total, item) => total + item.price * item.quantity, 0));
  const isAllChecked = computed(() => validItems.value.length > 0 && validItems.value.every(item => item.checked));

  const mapItem = (item: CartItemResponse): CartItem => ({
    id: String(item.id),
    productId: Number(item.productId),
    name: item.productName,
    price: Number(item.price),
    quantity: Number(item.quantity),
    image: item.mainImage,
    merchantId: item.merchantId,
    merchantName: item.merchantName,
    checked: item.selected === 1,
    stock: Number(item.stock),
    invalid: Boolean(item.invalid),
    invalidReason: item.invalidReason,
    supportCustomize: Boolean(item.supportCustomize),
  });

  const loadCart = async () => {
    loading.value = true;
    try {
      const response = await request.get('/customer/cart/list');
      items.value = (response.data?.items || []).map(mapItem);
    } finally {
      loading.value = false;
    }
  };

  const addItem = async (productId: number, quantity: number) => {
    await request.post('/customer/cart/add', { productId, quantity });
    await loadCart();
  };

  const updateQuantity = async (id: string, quantity: number) => {
    const item = items.value.find(current => current.id === id);
    if (!item || quantity === item.quantity) return;
    const previousQuantity = item.quantity;
    item.quantity = quantity;
    try {
      await request.put(`/customer/cart/${id}`, { quantity });
      await loadCart();
    } catch (error) {
      item.quantity = previousQuantity;
      throw error;
    }
  };

  const setItemChecked = async (id: string, checked: boolean) => {
    const item = items.value.find(current => current.id === id);
    if (!item || item.invalid || item.checked === checked) return;
    item.checked = checked;
    try {
      await request.patch(`/customer/cart/${id}/selected`, { selected: checked });
    } catch (error) {
      item.checked = !checked;
      throw error;
    }
  };

  const setAllChecked = async (checked: boolean) => {
    const previous = items.value.map(item => ({ id: item.id, checked: item.checked }));
    validItems.value.forEach(item => { item.checked = checked; });
    try {
      await request.patch('/customer/cart/select-all', { selected: checked });
    } catch (error) {
      previous.forEach(({ id, checked: oldChecked }) => {
        const item = items.value.find(current => current.id === id);
        if (item) item.checked = oldChecked;
      });
      throw error;
    }
  };

  const selectOnlyProduct = async (productId: number) => {
    const requests = items.value
      .filter(item => !item.invalid && item.checked !== (item.productId === productId))
      .map(item => setItemChecked(item.id, item.productId === productId));
    await Promise.all(requests);
  };

  const removeItem = async (id: string) => {
    await request.delete(`/customer/cart/${id}`);
    items.value = items.value.filter(item => item.id !== id);
  };

  const clearCart = () => {
    items.value = [];
  };

  return {
    items,
    loading,
    totalCount,
    selectedCount,
    totalPrice,
    isAllChecked,
    loadCart,
    addItem,
    updateQuantity,
    setItemChecked,
    setAllChecked,
    selectOnlyProduct,
    removeItem,
    clearCart,
  };
});
