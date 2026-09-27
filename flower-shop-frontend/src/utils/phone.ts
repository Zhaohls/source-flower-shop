export const PHONE_REGEX = /^1\d{10}$/;

export const sanitizePhoneInput = (value: string | null | undefined) => (
  String(value || '').replace(/\D/g, '').slice(0, 11)
);

export const isValidPhone = (value: string | null | undefined) => PHONE_REGEX.test(String(value || '').trim());
