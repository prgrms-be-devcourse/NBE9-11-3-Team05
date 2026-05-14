import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatDate(dateValue: any): string {
  if (!dateValue) return "";
  
  if (typeof dateValue === 'string') {
    if (dateValue.includes(".")) {
      const cleanStr = dateValue.replace(/\.\s*/g, '-').replace(/-$/, '');
      const parsed = new Date(cleanStr);
      if (!isNaN(parsed.getTime())) {
        return parsed.toLocaleDateString("ko-KR");
      }
    }
  }

  if (Array.isArray(dateValue)) {
    return new Date(
      dateValue[0],
      (dateValue[1] || 1) - 1,
      dateValue[2] || 1,
      dateValue[3] || 0,
      dateValue[4] || 0,
      dateValue[5] || 0
    ).toLocaleDateString("ko-KR");
  }
  const parsed = new Date(dateValue);
  if (isNaN(parsed.getTime())) return typeof dateValue === 'string' ? dateValue : "날짜 없음";
  return parsed.toLocaleDateString("ko-KR");
}

export function getDateTimeMs(dateValue: any): number {
  if (!dateValue) return 0;
  
  if (typeof dateValue === 'string') {
    if (dateValue.includes(".")) {
      const cleanStr = dateValue.replace(/\.\s*/g, '-').replace(/-$/, '');
      const parsed = new Date(cleanStr);
      if (!isNaN(parsed.getTime())) {
        return parsed.getTime();
      }
    }
  }

  if (Array.isArray(dateValue)) {
    return new Date(
      dateValue[0],
      (dateValue[1] || 1) - 1,
      dateValue[2] || 1,
      dateValue[3] || 0,
      dateValue[4] || 0,
      dateValue[5] || 0
    ).getTime();
  }
  const parsed = new Date(dateValue);
  return isNaN(parsed.getTime()) ? 0 : parsed.getTime();
}
