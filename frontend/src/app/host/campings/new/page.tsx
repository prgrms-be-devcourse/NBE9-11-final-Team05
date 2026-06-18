import HostCampingForm from "@/components/host/HostCampingForm";

export default function HostCampingNewPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">캠핑장 등록</h1>
        <p className="mt-1 text-sm text-gray-500">
          호스트로 운영할 캠핑장 정보를 입력해주세요.
        </p>
      </div>

      <HostCampingForm mode="create" />
    </div>
  );
}