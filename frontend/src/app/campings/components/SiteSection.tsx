import { Site } from "@/app/types/site";

interface Props {
  sites: Site[];
}

export default function SiteSection({
  sites,
}: Props) {

  return (
    <section>

      <h2 className="text-2xl font-bold mb-6">
        캠핑 구역
      </h2>

      <div className="space-y-5">

        {sites.map((site) => (

          <div
            key={site.name}
            className="
              bg-white
              rounded-3xl
              shadow-md
              p-6
              flex
              justify-between
            "
          >

            <div>

              <h3 className="font-bold text-xl">
                {site.name}
              </h3>

              <p className="mt-2 text-gray-500">
                기준 {site.baseCapacity}인 · 최대 {site.maxCapacity}인
              </p>

              <p className="mt-2 text-gray-500">
                {site.description}
              </p>

            </div>

            <div className="text-right">

              <div className="text-2xl font-bold">
                ₩{site.price.toLocaleString()}
              </div>

              <div className="mt-2 text-gray-400">
                총 {site.totalCount}개
              </div>

            </div>

          </div>

        ))}

      </div>

    </section>
  );
}