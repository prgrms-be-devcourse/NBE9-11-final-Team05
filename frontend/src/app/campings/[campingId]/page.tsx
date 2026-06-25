import CampingDescription from "@/components/camping/CampingDescription";
import CampingImageSlider from "@/components/camping/CampingImageSlider";
import CampingSummary from "@/components/camping/CampingSummary";
import FacilitySection from "@/components/camping/FacilitySection";
import ReservationCard from "@/components/camping/ReservationCard";
import ReviewSection from "@/components/camping/ReviewSection";
import SiteSection from "@/components/camping/SiteSection";
import ChatRoomEntry from "@/components/chat/ChatRoomEntry";
import { getCampingDetail } from "@/lib/api/reservation";
import { canReserve } from "@/lib/utils/auth";

interface Props {
  params: Promise<{
    campingId: string;
  }>;
}

export default async function CampingDetailPage({ params }: Props) {
  const { campingId } = await params;
  const id = Number(campingId);

  if (isNaN(id)) {
    throw new Error("올바르지 않은 캠핑장 ID입니다.");
  }

  const [camping, reservable] = await Promise.all([
    getCampingDetail(id),
    canReserve(),
  ]);

  return (
    <div className="max-w-7xl mx-auto px-6 py-10">
      <CampingImageSlider
        firstImageUrl={camping.firstImageUrl}
        imageUrls={camping.imageUrls}
        name={camping.name}
      />

      <div className="grid lg:grid-cols-[2fr_1fr] gap-12 mt-12">
        <div className="space-y-12">
          <CampingSummary camping={camping} />
          <FacilitySection />
          <ChatRoomEntry campingId={id} />
          <CampingDescription camping={camping} />
          <SiteSection sites={camping.sites} />
          <ReviewSection />
        </div>

        {/* USER만 예약 카드 표시, 호스트/관리자/비로그인은 숨김 */}
        {reservable ? (
          <ReservationCard camping={camping} />
        ) : (
          <aside />
        )}
      </div>
    </div>
  );
}