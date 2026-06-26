"use client";

import { useEffect, useState } from "react";
import { AdminUserResponse } from "@/types/admin";
import { getUsers, banUser, unbanUser } from "@/lib/api/admin";

const ROLES = ["전체", "USER", "HOST", "ADMIN"];
const STATUSES = ["전체", "ACTIVE", "BANNED"];

const ROLE_LABEL: Record<string, string> = {
    USER: "일반",
    HOST: "호스트",
    ADMIN: "관리자",
};

const STATUS_BADGE: Record<string, string> = {
    ACTIVE: "bg-green-100 text-green-700",
    BANNED: "bg-red-100 text-red-700",
};

export default function MembersPage() {
    const [users, setUsers] = useState<AdminUserResponse[]>([]);
    const [totalElements, setTotalElements] = useState(0);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const [role, setRole] = useState("전체");
    const [status, setStatus] = useState("전체");
    const [includeDeleted, setIncludeDeleted] = useState(false);
    const [keyword, setKeyword] = useState("");
    const [search, setSearch] = useState("");

    const [loading, setLoading] = useState(false);

    const fetchUsers = async (currentPage = 0) => {
        setLoading(true);
        try {
            const data = await getUsers({
                role: role === "전체" ? undefined : role,
                status: status === "전체" ? undefined : status,
                includeDeleted,
                keyword: search || undefined,
                page: currentPage,
                size: 10,
            });
            setUsers(data.content);
            setTotalElements(data.totalElements);
            setTotalPages(data.totalPages);
            setPage(currentPage);
        } catch (e) {
            console.error(e);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers(0);
    }, [role, status, includeDeleted]);

    const handleSearch = () => {
        setSearch(keyword);
        fetchUsers(0);
    };

    const handleBan = async (userId: number) => {
        if (!confirm("정말 정지하시겠습니까?")) return;
        await banUser(userId);
        fetchUsers(page);
    };

    const handleUnban = async (userId: number) => {
        if (!confirm("정지를 해제하시겠습니까?")) return;
        await unbanUser(userId);
        fetchUsers(page);
    };

    return (
        <div className="bg-[#F5F6F2] min-h-screen p-8">
            {/* 제목 */}
            <div className="mb-8">
                <h1 className="text-4xl font-bold text-[#2F3A2F]">회원 관리</h1>
                <p className="mt-2 text-gray-500">
                    전체 회원 {totalElements.toLocaleString()}명
                </p>
            </div>

            {/* 필터 */}
            <div className="bg-white rounded-2xl p-6 mb-6 shadow-sm border border-gray-100">
                <div className="flex flex-col gap-4">

                    {/* 역할 + 상태 필터 */}
                    <div className="flex flex-wrap gap-6 items-center">
                        <div className="flex items-center gap-2">
                            <span className="text-sm text-gray-500 w-10">역할</span>
                            <div className="w-px h-5 bg-gray-200" />  {/* 구분선 */}
                            <div className="flex gap-2">
                                {ROLES.map((r) => (
                                    <button
                                        key={r}
                                        onClick={() => setRole(r)}
                                        className={`px-4 py-1.5 rounded-full text-sm font-medium transition ${role === r
                                            ? "bg-[#5C7A5C] text-white"
                                            : "bg-gray-100 text-gray-600 hover:bg-gray-200"
                                            }`}
                                    >
                                        {r}
                                    </button>
                                ))}
                            </div>
                        </div>

                        <div className="flex items-center gap-2">
                            <span className="text-sm text-gray-500 w-10">상태</span>
                            <div className="w-px h-5 bg-gray-200" />  {/* 구분선 */}
                            <div className="flex gap-2">
                                {STATUSES.map((s) => (
                                    <button
                                        key={s}
                                        onClick={() => setStatus(s)}
                                        className={`px-4 py-1.5 rounded-full text-sm font-medium transition ${status === s
                                            ? "bg-[#5C7A5C] text-white"
                                            : "bg-gray-100 text-gray-600 hover:bg-gray-200"
                                            }`}
                                    >
                                        {s === "ACTIVE" ? "정상" : s === "BANNED" ? "정지" : s}
                                    </button>
                                ))}
                            </div>
                        </div>
                    </div>

                    {/* 검색 + 탈퇴 포함 */}
                    <div className="flex items-center gap-4">
                        <label className="flex items-center gap-2 text-sm text-gray-600 cursor-pointer">
                            <input
                                type="checkbox"
                                checked={includeDeleted}
                                onChange={(e) => setIncludeDeleted(e.target.checked)}
                                className="w-4 h-4 accent-[#5C7A5C]"
                            />
                            탈퇴 회원 포함
                        </label>

                        <div className="flex gap-2 ml-auto">
                            <input
                                type="text"
                                value={keyword}
                                onChange={(e) => setKeyword(e.target.value)}
                                onKeyDown={(e) => e.key === "Enter" && handleSearch()}
                                placeholder="이름, 이메일, 닉네임 검색"
                                className="px-4 py-2 border border-gray-200 rounded-xl text-sm w-64 focus:outline-none focus:border-[#5C7A5C]"
                            />
                            <button
                                onClick={handleSearch}
                                className="px-4 py-2 bg-[#5C7A5C] text-white rounded-xl text-sm hover:bg-[#4a6349] transition"
                            >
                                검색
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* 테이블 */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
                {loading ? (
                    <div className="flex items-center justify-center h-48 text-gray-400">
                        불러오는 중...
                    </div>
                ) : users.length === 0 ? (
                    <div className="flex items-center justify-center h-48 text-gray-400">
                        회원이 없습니다.
                    </div>
                ) : (
                    <table className="w-full text-sm">
                        <thead className="bg-gray-50 text-gray-500 border-b">
                            <tr>
                                <th className="px-6 py-4 text-left font-medium">이름</th>
                                <th className="px-6 py-4 text-left font-medium">이메일</th>
                                <th className="px-6 py-4 text-left font-medium">닉네임</th>
                                <th className="px-6 py-4 text-left font-medium">역할</th>
                                <th className="px-6 py-4 text-left font-medium">상태</th>
                                <th className="px-6 py-4 text-left font-medium">가입일</th>
                                <th className="px-6 py-4 text-left font-medium">관리</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-50">
                            {users.map((user) => (
                                <tr key={user.userId} className={`hover:bg-gray-50 transition ${user.isDeleted ? "opacity-50" : ""}`}>
                                    <td className="px-6 py-4 font-medium text-gray-800">
                                        {user.name}
                                        {user.isDeleted && (
                                            <span className="ml-2 text-xs text-gray-400">(탈퇴)</span>
                                        )}
                                    </td>
                                    <td className="px-6 py-4 text-gray-600">{user.email}</td>
                                    <td className="px-6 py-4 text-gray-600">{user.nickname}</td>
                                    <td className="px-6 py-4">
                                        <span className="px-2 py-1 bg-gray-100 text-gray-600 rounded-full text-xs">
                                            {ROLE_LABEL[user.role] ?? user.role}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4">
                                        {user.isDeleted ? (
                                            <span className="px-2 py-1 bg-gray-100 text-gray-500 rounded-full text-xs">탈퇴</span>
                                        ) : (
                                            <span className={`px-2 py-1 rounded-full text-xs ${STATUS_BADGE[user.status]}`}>
                                                {user.status === "ACTIVE" ? "정상" : "정지"}
                                            </span>
                                        )}
                                    </td>
                                    <td className="px-6 py-4 text-gray-500">
                                        {new Date(user.createdAt).toLocaleDateString("ko-KR")}
                                    </td>
                                    <td className="px-6 py-4">
                                        {!user.isDeleted && (
                                            user.status === "ACTIVE" ? (
                                                <button
                                                    onClick={() => handleBan(user.userId)}
                                                    className="px-3 py-1 bg-red-50 text-red-500 rounded-lg text-xs hover:bg-red-100 transition"
                                                >
                                                    정지
                                                </button>
                                            ) : (
                                                <button
                                                    onClick={() => handleUnban(user.userId)}
                                                    className="px-3 py-1 bg-green-50 text-green-600 rounded-lg text-xs hover:bg-green-100 transition"
                                                >
                                                    정지 해제
                                                </button>
                                            )
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}

                {/* 페이지네이션 */}
                {totalPages > 1 && (
                    <div className="flex justify-center items-center gap-2 py-4 border-t">
                        <button
                            onClick={() => fetchUsers(page - 1)}
                            disabled={page === 0}
                            className="px-3 py-1 rounded-lg text-sm text-gray-500 hover:bg-gray-100 disabled:opacity-30 transition"
                        >
                            이전
                        </button>

                        {Array.from({ length: totalPages }, (_, i) => i)
                            .filter((i) => i >= page - 2 && i <= page + 2)
                            .map((i) => (
                                <button
                                    key={i}
                                    onClick={() => fetchUsers(i)}
                                    className={`w-8 h-8 rounded-full text-sm transition ${page === i
                                        ? "bg-[#5C7A5C] text-white"
                                        : "text-gray-500 hover:bg-gray-100"
                                        }`}
                                >
                                    {i + 1}
                                </button>
                            ))}

                        <button
                            onClick={() => fetchUsers(page + 1)}
                            disabled={page === totalPages - 1}
                            className="px-3 py-1 rounded-lg text-sm text-gray-500 hover:bg-gray-100 disabled:opacity-30 transition"
                        >
                            다음
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
}