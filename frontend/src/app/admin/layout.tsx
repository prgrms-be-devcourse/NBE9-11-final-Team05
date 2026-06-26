"use client";

import { useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import { useAuthStore } from "@/stores/authStore";

export default function AdminLayout({
    children,
}: {
    children: React.ReactNode;
}) {
    const router = useRouter();
    const pathname = usePathname();
    const { isLoggedIn, role } = useAuthStore();
    const [mounted, setMounted] = useState(false);

    useEffect(() => {
        setMounted(true);
    }, []);

    useEffect(() => {
        if (mounted && (!isLoggedIn || role !== "ADMIN")) {
            if (!pathname.startsWith("/admin/login")) {
                router.push("/admin/login");
            }
        }
    }, [mounted, isLoggedIn, role, router, pathname]);

    if (!mounted) return null;

    if (!isLoggedIn || role !== "ADMIN") {
        if (pathname.startsWith("/admin/login")) {
            return <>{children}</>;
        }
        return null;
    }

    return <>{children}</>;
}