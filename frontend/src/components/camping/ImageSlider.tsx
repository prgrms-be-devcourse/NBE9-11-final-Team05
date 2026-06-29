// components/camping/ImageSlider.tsx
"use client";

import { useState } from "react";

interface Props {
    images: string[];
    name: string;
}

export default function ImageSlider({ images, name }: Props) {
    const [current, setCurrent] = useState(0);

    if (images.length === 0) {
        return (
            <div className="w-full h-full flex items-center justify-center text-gray-300 text-sm bg-gray-100">
                이미지 없음
            </div>
        );
    }

    if (images.length === 1) {
        return (
            <img
                src={images[0]}
                alt={name}
                className="w-full h-full object-cover"
            />
        );
    }

    return (
        <div className="relative w-full h-full group">
            <img
                src={images[current]}
                alt={name}
                className="w-full h-full object-cover"
            />

            {/* 이전 버튼 */}
            <button
                onClick={(e) => {
                    e.preventDefault();
                    setCurrent((prev) => (prev - 1 + images.length) % images.length);
                }}
                className="absolute left-2 top-1/2 -translate-y-1/2 w-7 h-7 bg-white/80 rounded-full flex items-center justify-center text-gray-700 opacity-0 group-hover:opacity-100 transition hover:bg-white"
            >
                ‹
            </button>

            {/* 다음 버튼 */}
            <button
                onClick={(e) => {
                    e.preventDefault();
                    setCurrent((prev) => (prev + 1) % images.length);
                }}
                className="absolute right-2 top-1/2 -translate-y-1/2 w-7 h-7 bg-white/80 rounded-full flex items-center justify-center text-gray-700 opacity-0 group-hover:opacity-100 transition hover:bg-white"
            >
                ›
            </button>

            {/* 인디케이터 */}
            <div className="absolute bottom-2 left-1/2 -translate-x-1/2 flex gap-1">
                {images.map((_, i) => (
                    <button
                        key={i}
                        onClick={(e) => {
                            e.preventDefault();
                            setCurrent(i);
                        }}
                        className={`w-1.5 h-1.5 rounded-full transition ${i === current ? "bg-white" : "bg-white/50"
                            }`}
                    />
                ))}
            </div>
        </div>
    );
}