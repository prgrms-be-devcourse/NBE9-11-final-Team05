import { SelectHTMLAttributes, forwardRef } from "react";
import { clsx } from "@/lib/utils/clsx";

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string;
  error?: string;
}

const Select = forwardRef<HTMLSelectElement, SelectProps>(
  ({ label, error, className, id, children, ...rest }, ref) => {
    const selectId = id ?? rest.name;
    return (
      <div className="flex flex-col gap-1.5">
        <label htmlFor={selectId} className="text-sm font-medium text-stone-700">
          {label}
        </label>
        <select
          ref={ref}
          id={selectId}
          className={clsx(
            "rounded-full border border-stone-200 bg-white px-4 py-2.5 text-sm text-stone-800 outline-none transition-colors focus:border-emerald-700 focus:ring-2 focus:ring-emerald-100",
            error && "border-red-400 focus:border-red-400 focus:ring-red-100",
            className
          )}
          {...rest}
        >
          {children}
        </select>
        {error && <p className="text-xs text-red-600">{error}</p>}
      </div>
    );
  }
);
Select.displayName = "Select";

export default Select;
