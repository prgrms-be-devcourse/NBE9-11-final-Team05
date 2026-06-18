"use client";

import { useRef } from "react";
import type { Swiper as SwiperType } from "swiper";

import { Swiper, SwiperSlide } from "swiper/react";
import { Pagination } from "swiper/modules";

import { ChevronLeft, ChevronRight } from "lucide-react";

import "swiper/css";
import "swiper/css/pagination";

interface Props {
  firstImageUrl: string | null;
  imageUrls: string[];
  name: string;
}

export default function CampingImageSlider({
  firstImageUrl,
  imageUrls,
  name,
}: Props) {

  const swiperRef = useRef<SwiperType | null>(null);

  const images = [
    firstImageUrl,
    ...imageUrls,
  ].filter((image): image is string => !!image);

  return (
    <section className="relative">

      <Swiper
        modules={[Pagination]}
        pagination={{
          clickable: true,
        }}
        onSwiper={(swiper) => {
          swiperRef.current = swiper;
        }}
        className="camping-swiper rounded-[32px]"
      >
        {images.map((image, index) => (
          <SwiperSlide key={index}>
            <img
              src={image}
              alt={name}
              className={`h-[400px] md:h-[500px] w-full rounded-[32px] object-cover`}
            />
          </SwiperSlide>
        ))}
      </Swiper>

      {/* 이전 버튼 */}
      <button
        onClick={() => swiperRef.current?.slidePrev()}
        className={`
          absolute
          left-5
          top-1/2
          -translate-y-1/2
          z-10

          w-12
          h-12

          bg-white/90
          backdrop-blur

          rounded-full

          shadow-lg

          flex
          items-center
          justify-center

          hover:scale-105
          transition
        `}
      >
        <ChevronLeft size={20} color="#4B6945" />
      </button>

      {/* 다음 버튼 */}
      <button
        onClick={() => swiperRef.current?.slideNext()}
        className={`absolute right-5 top-1/2 -translate-y-1/2 z-10
            w-12 h-12 bg-white/90 backdrop-blur rounded-full shadow-lg
            flex items-center justify-center
            hover:scale-105 transition`}
      >
        <ChevronRight size={20} color="#4B6945" />
      </button>

    </section>
  );
}