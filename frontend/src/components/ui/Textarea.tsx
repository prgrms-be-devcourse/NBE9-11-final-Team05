import { TextareaHTMLAttributes, forwardRef } from "react";
import { clsx } from "@/lib/utils/clsx";

interface TextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label: string;
  error?: string;
}

const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(
  ({ label, error, className, id, ...rest }, ref) => {
    const textareaId = id ?? rest.name;
    return (
      <div className="flex flex-col gap-1.5">
        <label htmlFor={textareaId} className="text-sm font-medium text-stone-700">
          {label}
        </label>
        <textarea
          ref={ref}
          id={textareaId}
          className={clsx(
            "rounded-2xl border border-stone-200 bg-white px-4 py-3 text-sm text-stone-800 placeholder:text-stone-400 outline-none transition-colors focus:border-emerald-700 focus:ring-2 focus:ring-emerald-100",
            error && "border-red-400 focus:border-red-400 focus:ring-red-100",
            className
          )}
          {...rest}
        />
        {error && <p className="text-xs text-red-600">{error}</p>}
      </div>
    );
  }
);
Textarea.displayName = "Textarea";

export default Textarea;
