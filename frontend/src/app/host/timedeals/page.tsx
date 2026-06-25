"use client";

import { useState, useEffect, useCallback } from "react";

// ── 타입 ──────────────────────────────────────
type TimeDealStatus = "SCHEDULED" | "ACTIVE" | "SOLD_OUT" | "ENDED" | "CANCELLED";

interface TimeDealResponse {
  id: number;
  siteId: number;
  siteName: string;
  campingId: number;
  campingName: string;
  checkIn: string;
  checkOut: string;
  quantity: number;
  soldCount: number;
  remaining: number;
  originalPrice: number;
  dealPrice: number;
  discountRate: number;
  saleStartAt: string;
  saleEndAt: string;
  status: TimeDealStatus;
}

interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

interface CreateForm {
  siteId: string;
  checkIn: string;
  checkOut: string;
  quantity: string;
  dealPrice: string;
  saleStartAt: string;
  saleEndAt: string;
}

interface UpdateForm {
  quantity: string;
  dealPrice: string;
  saleStartAt: string;
  saleEndAt: string;
}

interface HostCampingListResponse {
  id: number;
  name: string;
  address: string;
  status: string;
}

interface HostSiteResponse {
  id: number;
  name: string;
  description: string;
  baseCapacity: number;
  maxCapacity: number;
  totalAmount: number;
  price: number;
}

// ── 상수 ──────────────────────────────────────
const STATUS_META: Record<
  TimeDealStatus,
  { label: string; color: string; dot: string }
> = {
  SCHEDULED: {
    label: "판매 예정",
    color: "bg-amber-50 text-amber-700 border border-amber-200",
    dot: "bg-amber-400",
  },
  ACTIVE: {
    label: "판매 중",
    color: "bg-emerald-50 text-emerald-700 border border-emerald-200",
    dot: "bg-emerald-400",
  },
  SOLD_OUT: {
    label: "매진",
    color: "bg-red-50 text-red-700 border border-red-200",
    dot: "bg-red-400",
  },
  ENDED: {
    label: "종료",
    color: "bg-stone-100 text-stone-500 border border-stone-200",
    dot: "bg-stone-400",
  },
  CANCELLED: {
    label: "취소됨",
    color: "bg-stone-100 text-stone-400 border border-stone-200",
    dot: "bg-stone-300",
  },
};

const API_BASE = process.env.NEXT_PUBLIC_API_URL;

// ── API 함수 ──────────────────────────────────
async function fetchMyDeals(page: number): Promise<PageResponse<TimeDealResponse>> {
  const res = await fetch(`${API_BASE}/api/timedeals/host/my?page=${page}&size=10`, {
    credentials: "include",
  });
  if (!res.ok) throw new Error("목록 조회 실패");
  const json = await res.json();
  return json.data;
}

async function createDeal(body: object): Promise<TimeDealResponse> {
  const res = await fetch(`${API_BASE}/api/timedeals/host`, {
    method: "POST",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  const json = await res.json();
  if (!res.ok) throw new Error(json.message ?? "생성 실패");
  return json.data;
}

async function updateDeal(id: number, body: object): Promise<TimeDealResponse> {
  const res = await fetch(`${API_BASE}/api/timedeals/host/${id}`, {
    method: "PATCH",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  const json = await res.json();
  if (!res.ok) throw new Error(json.message ?? "수정 실패");
  return json.data;
}

async function cancelDeal(id: number): Promise<void> {
  const res = await fetch(`${API_BASE}/api/timedeals/host/${id}/cancel`, {
    method: "PATCH",
    credentials: "include",
  });
  if (!res.ok) throw new Error("취소 실패");
}

async function deleteDeal(id: number): Promise<void> {
  const res = await fetch(`${API_BASE}/api/timedeals/host/${id}`, {
    method: "DELETE",
    credentials: "include",
  });
  if (!res.ok) {
    const json = await res.json();
    throw new Error(json.message ?? "삭제 실패");
  }
}

async function fetchMyCampings(): Promise<HostCampingListResponse[]> {
  const res = await fetch(`${API_BASE}/api/host/campings`, { credentials: "include" });
  if (!res.ok) throw new Error("캠핑장 목록 조회 실패");
  const json = await res.json();
  return json.data;
}

async function fetchSitesByCamping(campingId: number): Promise<HostSiteResponse[]> {
  const res = await fetch(`${API_BASE}/api/host/campings/${campingId}/sites`, {
    credentials: "include",
  });
  if (!res.ok) throw new Error("사이트 목록 조회 실패");
  const json = await res.json();
  return json.data;
}

// ── 유틸 ──────────────────────────────────────
const fmtPrice = (n: number) => n.toLocaleString("ko-KR") + "원";
const fmtDate = (s: string) => s.replace("T", " ").substring(0, 16);
const fmtDay = (s: string) => s;

function progressPct(sold: number, qty: number) {
  if (qty === 0) return 0;
  return Math.min(100, Math.round((sold / qty) * 100));
}

// ── 서브 컴포넌트 ─────────────────────────────

function StatusBadge({ status }: { status: TimeDealStatus }) {
  const m = STATUS_META[status];
  return (
    <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${m.color}`}>
      <span className={`w-1.5 h-1.5 rounded-full ${m.dot}`} />
      {m.label}
    </span>
  );
}

function Toast({
  message,
  type,
  onClose,
}: {
  message: string;
  type: "success" | "error";
  onClose: () => void;
}) {
  useEffect(() => {
    const t = setTimeout(onClose, 3000);
    return () => clearTimeout(t);
  }, [onClose]);

  return (
    <div
      className={`fixed bottom-6 right-6 z-50 flex items-center gap-3 px-5 py-3.5 rounded-xl shadow-lg text-sm font-medium
        ${type === "success" ? "bg-[#1a3a2a] text-emerald-100" : "bg-red-900 text-red-100"}`}
    >
      <span>{type === "success" ? "✓" : "✕"}</span>
      {message}
      <button onClick={onClose} className="ml-2 opacity-60 hover:opacity-100 text-lg leading-none">×</button>
    </div>
  );
}

function ConfirmModal({
  title,
  description,
  confirmLabel,
  danger,
  onConfirm,
  onCancel,
}: {
  title: string;
  description: string;
  confirmLabel: string;
  danger?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}) {
  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/40 backdrop-blur-sm">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm mx-4 p-6">
        <h3 className="text-base font-semibold text-stone-800">{title}</h3>
        <p className="mt-2 text-sm text-stone-500 leading-relaxed">{description}</p>
        <div className="mt-6 flex gap-2 justify-end">
          <button
            onClick={onCancel}
            className="px-4 py-2 text-sm rounded-lg border border-stone-200 text-stone-600 hover:bg-stone-50 transition-colors"
          >
            취소
          </button>
          <button
            onClick={onConfirm}
            className={`px-4 py-2 text-sm rounded-lg font-medium text-white transition-colors
              ${danger ? "bg-red-600 hover:bg-red-700" : "bg-[#1a3a2a] hover:bg-[#142e21]"}`}
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}

function DealFormModal({
  mode,
  initial,
  onSubmit,
  onClose,
}: {
  mode: "create" | "edit";
  initial?: TimeDealResponse;
  onSubmit: (data: CreateForm | UpdateForm) => Promise<void>;
  onClose: () => void;
}) {
  // ── 캠핑장/사이트 선택 상태 (create 전용) ──
  const [campings, setCampings] = useState<HostCampingListResponse[]>([]);
  const [sites, setSites] = useState<HostSiteResponse[]>([]);
  const [selectedCampingId, setSelectedCampingId] = useState<number | null>(
    initial?.campingId ?? null
  );
  const [selectedSite, setSelectedSite] = useState<HostSiteResponse | null>(null);
  const [campingLoading, setCampingLoading] = useState(false);
  const [siteLoading, setSiteLoading] = useState(false);

  // ── 폼 필드 상태 ──
  const [form, setForm] = useState({
    checkIn: initial?.checkIn ?? "",
    checkOut: initial?.checkOut ?? "",
    quantity: initial?.quantity.toString() ?? "",
    dealPrice: initial?.dealPrice.toString() ?? "",
    saleStartAt: initial?.saleStartAt?.substring(0, 16) ?? "",
    saleEndAt: initial?.saleEndAt?.substring(0, 16) ?? "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const setField = (key: keyof typeof form) =>
    (e: React.ChangeEvent<HTMLInputElement>) =>
      setForm((prev) => ({ ...prev, [key]: e.target.value }));

  // create 모드일 때 캠핑장 목록 로드
  useEffect(() => {
    if (mode !== "create") return;
    setCampingLoading(true);
    fetchMyCampings()
      .then(setCampings)
      .catch(() => setError("캠핑장 목록을 불러오지 못했습니다."))
      .finally(() => setCampingLoading(false));
  }, [mode]);

  // 캠핑장 선택 시 사이트 목록 로드
  useEffect(() => {
    if (!selectedCampingId) {
      setSites([]);
      setSelectedSite(null);
      return;
    }
    setSiteLoading(true);
    setSelectedSite(null);
    fetchSitesByCamping(selectedCampingId)
      .then(setSites)
      .catch(() => setError("사이트 목록을 불러오지 못했습니다."))
      .finally(() => setSiteLoading(false));
  }, [selectedCampingId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (mode === "create" && !selectedSite) {
      setError("사이트를 선택해주세요.");
      return;
    }
    setLoading(true);
    setError("");
    try {
      const payload =
        mode === "create"
          ? {
              siteId: selectedSite!.id,
              checkIn: form.checkIn,
              checkOut: form.checkOut,
              quantity: Number(form.quantity),
              dealPrice: Number(form.dealPrice),
              saleStartAt: form.saleStartAt,
              saleEndAt: form.saleEndAt,
            }
          : {
              quantity: Number(form.quantity),
              dealPrice: Number(form.dealPrice),
              saleStartAt: form.saleStartAt,
              saleEndAt: form.saleEndAt,
            };
      await onSubmit(payload as unknown as CreateForm | UpdateForm);
      onClose();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const inputCls =
    "mt-1 block w-full px-3 py-2.5 rounded-lg border border-stone-200 text-sm text-stone-800 " +
    "focus:outline-none focus:ring-2 focus:ring-[#4a7c59]/40 focus:border-[#4a7c59] transition-colors";
  const labelCls = "text-xs font-medium text-stone-500 uppercase tracking-wide";

  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/40 backdrop-blur-sm">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md mx-4 max-h-[90vh] overflow-y-auto">

        {/* 헤더 */}
        <div className="flex items-center justify-between px-6 pt-6 pb-4 border-b border-stone-100">
          <div>
            <p className="text-xs font-medium text-[#4a7c59] uppercase tracking-widest mb-0.5">
              타임딜 {mode === "create" ? "등록" : "수정"}
            </p>
            <h2 className="text-lg font-semibold text-stone-800">
              {mode === "create" ? "새 타임딜 만들기" : `${initial?.siteName} 수정`}
            </h2>
          </div>
          <button
            onClick={onClose}
            className="text-stone-400 hover:text-stone-600 text-2xl leading-none transition-colors"
          >
            ×
          </button>
        </div>

        <form onSubmit={handleSubmit} className="px-6 py-5 space-y-5">

          {/* ── STEP 1: 캠핑장 선택 (create 전용) ── */}
          {mode === "create" && (
            <div>
              <p className={labelCls}>1단계 · 캠핑장 선택</p>
              {campingLoading ? (
                <div className="mt-2 flex gap-2">
                  {[1, 2].map((i) => (
                    <div key={i} className="h-10 flex-1 rounded-xl bg-stone-100 animate-pulse" />
                  ))}
                </div>
              ) : campings.length === 0 ? (
                <p className="mt-2 text-xs text-stone-400 py-3 text-center border border-stone-100 rounded-xl">
                  등록된 캠핑장이 없습니다
                </p>
              ) : (
                <div className="mt-2 flex flex-col gap-2">
                  {campings.map((c) => (
                    <button
                      key={c.id}
                      type="button"
                      onClick={() => setSelectedCampingId(c.id)}
                      className={`flex items-center justify-between px-4 py-3 rounded-xl border text-left transition-all
                        ${selectedCampingId === c.id
                          ? "border-[#4a7c59] bg-[#f0f7f3] ring-2 ring-[#4a7c59]/20"
                          : "border-stone-200 hover:border-stone-300 hover:bg-stone-50"
                        }`}
                    >
                      <div>
                        <p className="text-sm font-medium text-stone-800">{c.name}</p>
                        <p className="text-xs text-stone-400 mt-0.5">{c.address}</p>
                      </div>
                      {selectedCampingId === c.id && (
                        <span className="text-[#4a7c59] text-lg">✓</span>
                      )}
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* ── STEP 2: 사이트 선택 (create 전용) ── */}
          {mode === "create" && (
            <div>
              <p className={`${labelCls} ${!selectedCampingId ? "opacity-40" : ""}`}>
                2단계 · 사이트 선택
              </p>
              {!selectedCampingId ? (
                <p className="mt-2 text-xs text-stone-300 py-3 text-center border border-dashed border-stone-200 rounded-xl">
                  캠핑장을 먼저 선택해주세요
                </p>
              ) : siteLoading ? (
                <div className="mt-2 flex flex-col gap-2">
                  {[1, 2, 3].map((i) => (
                    <div key={i} className="h-14 rounded-xl bg-stone-100 animate-pulse" />
                  ))}
                </div>
              ) : sites.length === 0 ? (
                <p className="mt-2 text-xs text-stone-400 py-3 text-center border border-stone-100 rounded-xl">
                  등록된 사이트가 없습니다
                </p>
              ) : (
                <div className="mt-2 flex flex-col gap-2">
                  {sites.map((s) => (
                    <button
                      key={s.id}
                      type="button"
                      onClick={() => setSelectedSite(s)}
                      className={`flex items-center justify-between px-4 py-3 rounded-xl border text-left transition-all
                        ${selectedSite?.id === s.id
                          ? "border-[#4a7c59] bg-[#f0f7f3] ring-2 ring-[#4a7c59]/20"
                          : "border-stone-200 hover:border-stone-300 hover:bg-stone-50"
                        }`}
                    >
                      <div>
                        <p className="text-sm font-medium text-stone-800">{s.name}</p>
                        <p className="text-xs text-stone-400 mt-0.5">
                          기본 {s.baseCapacity}인 · 최대 {s.maxCapacity}인 · 재고 {s.totalAmount}개
                        </p>
                      </div>
                      <div className="text-right ml-3 shrink-0">
                        <p className="text-sm font-semibold text-[#1a3a2a]">
                          {s.price.toLocaleString("ko-KR")}원
                        </p>
                        {selectedSite?.id === s.id && (
                          <span className="text-[#4a7c59] text-lg">✓</span>
                        )}
                      </div>
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* ── 구분선 (create 전용) ── */}
          {mode === "create" && (
            <div className="flex items-center gap-3">
              <div className="flex-1 h-px bg-stone-100" />
              <p className={`${labelCls} ${!selectedSite ? "opacity-40" : ""}`}>
                3단계 · 타임딜 정보
              </p>
              <div className="flex-1 h-px bg-stone-100" />
            </div>
          )}

          {/* ── 날짜 (create 전용) ── */}
          {mode === "create" && (
            <fieldset disabled={!selectedSite} className="space-y-4 disabled:opacity-40">
              <div className="grid grid-cols-2 gap-3">
                <label className="block">
                  <span className={labelCls}>체크인</span>
                  <input
                    type="date"
                    value={form.checkIn}
                    onChange={setField("checkIn")}
                    required
                    className={inputCls}
                  />
                </label>
                <label className="block">
                  <span className={labelCls}>체크아웃</span>
                  <input
                    type="date"
                    value={form.checkOut}
                    onChange={setField("checkOut")}
                    required
                    className={inputCls}
                  />
                </label>
              </div>
            </fieldset>
          )}

          {/* ── 공통 필드 ── */}
          <fieldset
            disabled={mode === "create" && !selectedSite}
            className="space-y-4 disabled:opacity-40"
          >
            <div className="grid grid-cols-2 gap-3">
              <label className="block">
                <span className={labelCls}>수량</span>
                <input
                  type="number"
                  min={1}
                  max={selectedSite?.totalAmount}
                  value={form.quantity}
                  onChange={setField("quantity")}
                  placeholder={selectedSite ? `최대 ${selectedSite.totalAmount}개` : "수량"}
                  required
                  className={inputCls}
                />
              </label>
              <label className="block">
                <span className={labelCls}>타임딜 가격 (1박)</span>
                <input
                  type="number"
                  min={0}
                  value={form.dealPrice}
                  onChange={setField("dealPrice")}
                  placeholder={
                    selectedSite
                      ? `원가 ${selectedSite.price.toLocaleString()}원`
                      : mode === "edit"
                      ? `원가 ${initial?.originalPrice.toLocaleString()}원`
                      : "원"
                  }
                  required
                  className={inputCls}
                />
              </label>
            </div>

            {/* 할인율 미리보기 */}
            {form.dealPrice && (mode === "create" ? selectedSite : initial) && (
              <div className="flex items-center gap-2 px-3 py-2 bg-amber-50 rounded-lg border border-amber-100">
                <span className="text-xs text-amber-700">예상 할인율</span>
                <span className="text-sm font-bold text-amber-700">
                  {Math.max(
                    0,
                    Math.round(
                      (1 -
                        Number(form.dealPrice) /
                          (mode === "create"
                            ? selectedSite!.price
                            : initial!.originalPrice)) *
                        100
                    )
                  )}
                  %
                </span>
                <span className="text-xs text-amber-500 ml-auto">
                  {(mode === "create" ? selectedSite!.price : initial!.originalPrice).toLocaleString()}원
                  {" → "}
                  {Number(form.dealPrice).toLocaleString()}원
                </span>
              </div>
            )}

            <label className="block">
              <span className={labelCls}>판매 시작</span>
              <input
                type="datetime-local"
                value={form.saleStartAt}
                onChange={setField("saleStartAt")}
                required
                className={inputCls}
              />
            </label>
            <label className="block">
              <span className={labelCls}>판매 종료</span>
              <input
                type="datetime-local"
                value={form.saleEndAt}
                onChange={setField("saleEndAt")}
                required
                className={inputCls}
              />
            </label>
          </fieldset>

          {error && (
            <p className="text-xs text-red-600 bg-red-50 px-3 py-2 rounded-lg border border-red-100">
              {error}
            </p>
          )}

          <div className="flex gap-2 pt-1">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 py-2.5 rounded-xl border border-stone-200 text-sm text-stone-600 hover:bg-stone-50 transition-colors"
            >
              취소
            </button>
            <button
              type="submit"
              disabled={loading || (mode === "create" && !selectedSite)}
              className="flex-1 py-2.5 rounded-xl bg-[#1a3a2a] hover:bg-[#142e21] text-white text-sm font-medium
                disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {loading ? "처리 중…" : mode === "create" ? "등록하기" : "저장하기"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function DealCard({
  deal,
  onEdit,
  onCancel,
  onDelete,
}: {
  deal: TimeDealResponse;
  onEdit: () => void;
  onCancel: () => void;
  onDelete: () => void;
}) {
  const pct = progressPct(deal.soldCount, deal.quantity);
  const canEdit = deal.soldCount === 0 && deal.status !== "CANCELLED" && deal.status !== "ENDED";
  const canCancel = deal.status !== "ENDED" && deal.status !== "CANCELLED";
  const canDelete = deal.soldCount === 0;

  return (
    <div className="group bg-white rounded-2xl border border-stone-100 hover:border-[#4a7c59]/30 hover:shadow-md transition-all duration-200">
      {/* 상단 */}
      <div className="px-5 pt-5 pb-4">
        <div className="flex items-start justify-between gap-3 mb-3">
          <div className="min-w-0">
            <p className="text-xs text-stone-400 mb-0.5 truncate">{deal.campingName}</p>
            <h3 className="font-semibold text-stone-800 truncate">{deal.siteName}</h3>
          </div>
          <StatusBadge status={deal.status} />
        </div>

        {/* 날짜 */}
        <div className="flex items-center gap-2 text-xs text-stone-500 mb-4">
          <span className="font-medium text-stone-700">{fmtDay(deal.checkIn)}</span>
          <span className="text-stone-300">→</span>
          <span className="font-medium text-stone-700">{fmtDay(deal.checkOut)}</span>
        </div>

        {/* 가격 */}
        <div className="flex items-baseline gap-2 mb-1">
          <span className="text-xl font-bold text-[#1a3a2a]">{fmtPrice(deal.dealPrice)}</span>
          <span className="text-xs text-stone-400 line-through">{fmtPrice(deal.originalPrice)}</span>
          <span className="text-xs font-bold text-amber-600">{deal.discountRate}% 할인</span>
        </div>

        {/* 재고 바 */}
        <div className="mt-3">
          <div className="flex justify-between text-xs text-stone-500 mb-1.5">
            <span>판매 현황</span>
            <span>
              <span className="font-medium text-stone-700">{deal.soldCount}</span>
              <span className="text-stone-300"> / </span>
              {deal.quantity}개
            </span>
          </div>
          <div className="h-1.5 bg-stone-100 rounded-full overflow-hidden">
            <div
              className={`h-full rounded-full transition-all duration-500
                ${pct >= 100 ? "bg-red-400" : pct >= 70 ? "bg-amber-400" : "bg-[#4a7c59]"}`}
              style={{ width: `${pct}%` }}
            />
          </div>
        </div>

        {/* 판매 기간 */}
        <div className="mt-3 pt-3 border-t border-stone-50 space-y-1">
          <div className="flex items-center justify-between text-xs">
            <span className="text-stone-400">판매 시작</span>
            <span className="text-stone-600 font-medium">{fmtDate(deal.saleStartAt)}</span>
          </div>
          <div className="flex items-center justify-between text-xs">
            <span className="text-stone-400">판매 종료</span>
            <span className="text-stone-600 font-medium">{fmtDate(deal.saleEndAt)}</span>
          </div>
        </div>
      </div>

      {/* 하단 액션 */}
      <div className="px-4 pb-4 flex gap-2">
        {canEdit && (
          <button
            onClick={onEdit}
            className="flex-1 py-2 text-xs font-medium rounded-lg border border-[#4a7c59]/30 text-[#4a7c59]
              hover:bg-[#4a7c59]/5 transition-colors"
          >
            수정
          </button>
        )}
        {canCancel && (
          <button
            onClick={onCancel}
            className="flex-1 py-2 text-xs font-medium rounded-lg border border-amber-200 text-amber-700
              hover:bg-amber-50 transition-colors"
          >
            취소
          </button>
        )}
        {canDelete && (
          <button
            onClick={onDelete}
            className="flex-1 py-2 text-xs font-medium rounded-lg border border-red-100 text-red-500
              hover:bg-red-50 transition-colors"
          >
            삭제
          </button>
        )}
        {!canEdit && !canCancel && !canDelete && (
          <div className="flex-1 py-2 text-center text-xs text-stone-300">조치 없음</div>
        )}
      </div>
    </div>
  );
}

function EmptyState({ onCreateClick }: { onCreateClick: () => void }) {
  return (
    <div className="col-span-full flex flex-col items-center justify-center py-24 text-center">
      <div className="w-16 h-16 rounded-2xl bg-[#f0f7f3] flex items-center justify-center mb-4 text-3xl">
        🏕
      </div>
      <p className="text-stone-700 font-medium mb-1">등록된 타임딜이 없습니다</p>
      <p className="text-sm text-stone-400 mb-6">첫 번째 타임딜을 만들어 특가 캠핑을 제안해보세요</p>
      <button
        onClick={onCreateClick}
        className="px-5 py-2.5 bg-[#1a3a2a] text-white text-sm font-medium rounded-xl hover:bg-[#142e21] transition-colors"
      >
        첫 타임딜 등록하기
      </button>
    </div>
  );
}

function Pagination({
  current,
  total,
  onChange,
}: {
  current: number;
  total: number;
  onChange: (p: number) => void;
}) {
  if (total <= 1) return null;

  return (
    <div className="flex items-center justify-center gap-1 mt-8">
      <button
        onClick={() => onChange(current - 1)}
        disabled={current === 0}
        className="w-8 h-8 rounded-lg text-sm text-stone-500 hover:bg-stone-100 disabled:opacity-30 transition-colors"
      >
        ‹
      </button>
      {Array.from({ length: total }, (_, i) => (
        <button
          key={i}
          onClick={() => onChange(i)}
          className={`w-8 h-8 rounded-lg text-sm transition-colors
            ${i === current
              ? "bg-[#1a3a2a] text-white font-medium"
              : "text-stone-500 hover:bg-stone-100"
            }`}
        >
          {i + 1}
        </button>
      ))}
      <button
        onClick={() => onChange(current + 1)}
        disabled={current === total - 1}
        className="w-8 h-8 rounded-lg text-sm text-stone-500 hover:bg-stone-100 disabled:opacity-30 transition-colors"
      >
        ›
      </button>
    </div>
  );
}

// ── 필터 탭 ───────────────────────────────────
const FILTER_TABS: { key: TimeDealStatus | "ALL"; label: string }[] = [
  { key: "ALL", label: "전체" },
  { key: "ACTIVE", label: "판매 중" },
  { key: "SCHEDULED", label: "예정" },
  { key: "SOLD_OUT", label: "매진" },
  { key: "ENDED", label: "종료" },
  { key: "CANCELLED", label: "취소" },
];

// ── 메인 페이지 ───────────────────────────────
export default function TimeDealManagePage() {
  const [deals, setDeals] = useState<TimeDealResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<TimeDealStatus | "ALL">("ALL");

  // 모달 상태
  const [showCreate, setShowCreate] = useState(false);
  const [editTarget, setEditTarget] = useState<TimeDealResponse | null>(null);
  const [cancelTarget, setCancelTarget] = useState<TimeDealResponse | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<TimeDealResponse | null>(null);

  // 토스트
  const [toast, setToast] = useState<{ message: string; type: "success" | "error" } | null>(null);

  const showToast = (message: string, type: "success" | "error") =>
    setToast({ message, type });

  const load = useCallback(async (p: number) => {
    setLoading(true);
    try {
      const data = await fetchMyDeals(p);
      setDeals(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch {
      showToast("목록을 불러오지 못했습니다.", "error");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load(page);
  }, [page, load]);

  const filtered =
    filter === "ALL" ? deals : deals.filter((d) => d.status === filter);

  // 상태별 카운트
  const counts = deals.reduce(
    (acc, d) => ({ ...acc, [d.status]: (acc[d.status] ?? 0) + 1 }),
    {} as Record<string, number>
  );

  // 액션 핸들러
  const handleCreate = async (data: CreateForm | UpdateForm) => {
    await createDeal(data);
    showToast("타임딜이 등록되었습니다.", "success");
    load(page);
  };

  const handleEdit = async (data: CreateForm | UpdateForm) => {
    if (!editTarget) return;
    await updateDeal(editTarget.id, data);
    showToast("타임딜이 수정되었습니다.", "success");
    load(page);
  };

  const handleCancel = async () => {
    if (!cancelTarget) return;
    try {
      await cancelDeal(cancelTarget.id);
      showToast("타임딜이 취소되었습니다.", "success");
      load(page);
    } catch {
      showToast("취소에 실패했습니다.", "error");
    } finally {
      setCancelTarget(null);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    try {
      await deleteDeal(deleteTarget.id);
      showToast("타임딜이 삭제되었습니다.", "success");
      load(page);
    } catch (err: unknown) {
      showToast(err instanceof Error ? err.message : "삭제에 실패했습니다.", "error");
    } finally {
      setDeleteTarget(null);
    }
  };

  return (
    <div className="min-h-screen bg-[#f7f6f3]">
      {/* 헤더 */}
      <header className="bg-[#1a3a2a] px-6 py-5">
        <div className="max-w-6xl mx-auto flex items-center justify-between">
          <div>
            <p className="text-[#4a7c59] text-xs font-medium tracking-widest uppercase mb-0.5">
              캠핑가잣 호스트
            </p>
            <h1 className="text-white text-xl font-semibold">타임딜 관리</h1>
          </div>
          <button
            onClick={() => setShowCreate(true)}
            className="flex items-center gap-2 px-4 py-2.5 bg-[#4a7c59] hover:bg-[#3d6b4b] text-white text-sm font-medium
              rounded-xl transition-colors"
          >
            <span className="text-base leading-none">+</span>
            타임딜 등록
          </button>
        </div>
      </header>

      {/* 요약 스탯 */}
      <div className="bg-[#1a3a2a]/90 border-t border-white/5 px-6 pb-5">
        <div className="max-w-6xl mx-auto grid grid-cols-3 gap-4 pt-4">
          {[
            { label: "전체", value: totalElements },
            { label: "판매 중", value: counts["ACTIVE"] ?? 0 },
            { label: "이번 달 매진", value: counts["SOLD_OUT"] ?? 0 },
          ].map(({ label, value }) => (
            <div key={label} className="text-center">
              <p className="text-2xl font-bold text-white">{value}</p>
              <p className="text-xs text-[#4a7c59] mt-0.5">{label}</p>
            </div>
          ))}
        </div>
      </div>

      <main className="max-w-6xl mx-auto px-4 sm:px-6 py-8">
        {/* 필터 탭 */}
        <div className="flex gap-1 flex-wrap mb-6">
          {FILTER_TABS.map(({ key, label }) => {
            const count = key === "ALL" ? deals.length : (counts[key] ?? 0);
            return (
              <button
                key={key}
                onClick={() => setFilter(key)}
                className={`px-3.5 py-1.5 rounded-full text-sm font-medium transition-colors
                  ${filter === key
                    ? "bg-[#1a3a2a] text-white"
                    : "bg-white border border-stone-200 text-stone-600 hover:border-stone-300"
                  }`}
              >
                {label}
                {count > 0 && (
                  <span className={`ml-1.5 text-xs ${filter === key ? "text-[#4a7c59]" : "text-stone-400"}`}>
                    {count}
                  </span>
                )}
              </button>
            );
          })}
        </div>

        {/* 카드 그리드 */}
        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="bg-white rounded-2xl border border-stone-100 h-64 animate-pulse" />
            ))}
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {filtered.length === 0 ? (
              <EmptyState onCreateClick={() => setShowCreate(true)} />
            ) : (
              filtered.map((deal) => (
                <DealCard
                  key={deal.id}
                  deal={deal}
                  onEdit={() => setEditTarget(deal)}
                  onCancel={() => setCancelTarget(deal)}
                  onDelete={() => setDeleteTarget(deal)}
                />
              ))
            )}
          </div>
        )}

        <Pagination current={page} total={totalPages} onChange={setPage} />
      </main>

      {/* 모달들 */}
      {showCreate && (
        <DealFormModal
          mode="create"
          onSubmit={handleCreate}
          onClose={() => setShowCreate(false)}
        />
      )}
      {editTarget && (
        <DealFormModal
          mode="edit"
          initial={editTarget}
          onSubmit={handleEdit}
          onClose={() => setEditTarget(null)}
        />
      )}
      {cancelTarget && (
        <ConfirmModal
          title="타임딜을 취소하시겠습니까?"
          description={`"${cancelTarget.siteName}" 타임딜을 취소하면 더 이상 구매할 수 없습니다. 이 작업은 되돌릴 수 없습니다.`}
          confirmLabel="취소하기"
          danger
          onConfirm={handleCancel}
          onCancel={() => setCancelTarget(null)}
        />
      )}
      {deleteTarget && (
        <ConfirmModal
          title="타임딜을 삭제하시겠습니까?"
          description={`"${deleteTarget.siteName}" 타임딜을 영구적으로 삭제합니다. 판매 이력이 있는 경우 삭제할 수 없습니다.`}
          confirmLabel="삭제하기"
          danger
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
        />
      )}

      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}
    </div>
  );
}