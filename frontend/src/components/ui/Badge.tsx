import { clsx } from "@/lib/utils/clsx";

interface BadgeProps {
  tone?: "neutral" | "success" | "danger";
  children: React.ReactNode;
}

const TONE_CLASS: Record<NonNullable<BadgeProps["tone"]>, string> = {
  neutral: "bg-stone-100 text-stone-600",
  success: "bg-emerald-100 text-emerald-700",
  danger: "bg-red-100 text-red-600",
};

export default function Badge({ tone = "neutral", children }: BadgeProps) {
  return (
    <span
      className={clsx(
        "inline-flex items-center rounded-full px-3 py-1 text-xs font-medium",
        TONE_CLASS[tone]
      )}
    >
      {children}
    </span>
  );
}
