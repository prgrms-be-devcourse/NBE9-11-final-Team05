// app/campings/[campingId]/page.tsx

import { getCampingDetail } from "@/app/lib/camping";
import CampingImageSlider from "../components/CampingImageSlider";
import SiteSection from "../components/SiteSection";
import FacilitySection from "../components/FacilitySection";
import ReviewSection from "../components/ReviewSection";
import ReservationCard from "../components/ReservationCard";
import CampingSummary from "../components/CampingSummary";
import CampingDescription from "../components/CampingDescription";

interface Props {
  params: Promise<{
    campingId: string;
  }>;
}

export default async function CampingDetailPage(
  { params }: Props
) {

  const { campingId } = await params;

  const camping = await getCampingDetail(
    Number(campingId)
  );

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

          <CampingDescription camping={camping} />

          <SiteSection sites={camping.sites} />

          <ReviewSection />

        </div>

        <ReservationCard camping={camping} />

      </div>

    </div>
  );
}