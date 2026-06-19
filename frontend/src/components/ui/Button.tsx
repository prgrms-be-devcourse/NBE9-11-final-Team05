import { ButtonHTMLAttributes, forwardRef } from "react";
import { clsx } from "@/lib/utils/clsx";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "primary" | "secondary" | "ghost" | "danger-outline";
  fullWidth?: boolean;
}

const VARIANT_CLASS: Record<NonNullable<ButtonProps["variant"]>, string> = {
  primary:
    "bg-amber-600 text-white hover:bg-amber-700 disabled:bg-amber-300",
  secondary:
    "bg-stone-700 text-white hover:bg-stone-800 disabled:bg-stone-300",
  ghost:
    "bg-transparent text-stone-700 border border-stone-300 hover:bg-stone-50",
  "danger-outline":
    "bg-transparent text-red-600 border border-red-200 hover:bg-red-50",
};

const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  (
    { variant = "primary", fullWidth, className, disabled, children, ...rest },
    ref
  ) => {
    return (
      <button
        ref={ref}
        disabled={disabled}
        className={clsx(
          "inline-flex items-center justify-center rounded-full px-6 py-3 text-sm font-semibold transition-colors disabled:cursor-not-allowed",
          VARIANT_CLASS[variant],
          fullWidth && "w-full",
          className
        )}
        {...rest}
      >
        {children}
      </button>
    );
  }
);
Button.displayName = "Button";

export default Button;
