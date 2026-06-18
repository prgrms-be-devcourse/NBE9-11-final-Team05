import { CampingDetail } from "@/app/types/camping";

interface Props {
  camping: CampingDetail;
}
  
export default function CampingDescription({ camping }: Props) {
  return (
    <section className="mt-10">
      
      <h2 className="text-xl font-bold text-gray-900 mb-5">
        캠핑장 소개
      </h2>

      <div className="bg-white rounded-3xl shadow-md border border-gray-100 p-10">
        
        <p className="text-gray-600 leading-7 whitespace-pre-line">
          {camping.description}
        </p>

      </div>
    </section>
  );
}