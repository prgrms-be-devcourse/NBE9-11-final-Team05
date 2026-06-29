import { toast } from "sonner";

export const AppToast = {
  success: (msg: string) =>
    toast.success(msg, {
      style: {
        background: "#008000",
        color: "white",
        fontWeight: "bold",
      },
    }),

  error: (msg: string) =>
    toast.error(msg, {
      style: {
        background: "#c7383c",
        color: "white",
        fontWeight: "bold",
      },
    }),

  info: (msg: string) =>
    toast(msg, {
      style: {
        background: "#464646",
        color: "white",
      },
    }),
};