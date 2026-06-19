type ClassValue = string | number | boolean | null | undefined;
 
/** 외부 패키지 없이 className을 조건부로 합치는 미니 유틸 */
export function clsx(...values: ClassValue[]): string {
  return values.filter(Boolean).join(" ");
}
 