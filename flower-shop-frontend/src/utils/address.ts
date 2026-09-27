export const formatAddress = (address: {
  province?: string | null;
  city?: string | null;
  district?: string | null;
  detailAddress?: string | null;
}) => (
  [
    address.province,
    address.city,
    address.district,
    address.detailAddress
  ]
    .map(item => String(item || '').trim())
    .filter(Boolean)
    .join('')
);
