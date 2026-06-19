import { HTMLAttributes } from "react";
import { clsx } from "@/lib/utils/clsx";

function Card({ className, ...rest }: HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={clsx(
        "rounded-3xl border border-stone-100 bg-white p-6 shadow-sm",
        className
      )}
      {...rest}
    />
  );
}

export default Card;
