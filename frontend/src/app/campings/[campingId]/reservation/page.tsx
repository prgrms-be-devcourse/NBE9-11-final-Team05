import ReservationForm from "@/components/reservation/ReservationForm";
import { getCampingDetail } from "@/lib/api/reservation";

interface PageProps {
  params: Promise<{ campingId: string }>;
}

export default async function ReservationPage({ params }: PageProps) {
  const { campingId } = await params;

  const camping = await getCampingDetail(Number(campingId));

  const siteOptions = camping.sites.map((site) => ({
    id: site.id,
    name: `${site.name} (최대 ${site.maxCapacity}명 / ${site.price.toLocaleString()}원)`,
    price: site.price,
  }));

  return (
    <div className="p-6">
      <ReservationForm
        campingId={campingId}
        siteOptions={siteOptions}
        campingName={camping.name}
      />
    </div>
  );
}
