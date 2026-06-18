"use client";

import { FormEvent, useState } from "react";
import { addCampingImage } from "@/lib/api/host";

interface HostImageManagerProps {
  campingId: number;
  imageUrls: string[];
}

export default function HostImageManager({
  campingId,
  imageUrls,
}: HostImageManagerProps) {
  const [images, setImages] = useState<string[]>(imageUrls);
  const [imageUrl, setImageUrl] = useState("");

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!imageUrl.trim()) {
      alert("이미지 URL을 입력해주세요.");
      return;
    }

    try {
      const createdImage = await addCampingImage(campingId, {
        imageUrl,
      });

      setImages((prev) => [...prev, createdImage.imageUrl]);
      setImageUrl("");
    } catch {
      alert("이미지 등록에 실패했습니다.");
    }
  }

  return (
    <section className="space-y-4 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
      <h2 className="text-lg font-semibold text-gray-900">이미지 관리</h2>
  
      <form onSubmit={handleSubmit} className="flex gap-2">
        <input
          placeholder="이미지 URL"
          value={imageUrl}
          onChange={(e) => setImageUrl(e.target.value)}
          className="flex-1 rounded-md border border-gray-300 px-3 py-2 text-sm"
        />
  
        <button
          type="submit"
          className="rounded-md bg-gray-900 px-4 py-2 text-sm text-white hover:bg-gray-700"
        >
          이미지 추가
        </button>
      </form>
  
      <div className="grid gap-4 md:grid-cols-2">
        {images.length === 0 ? (
          <p className="text-sm text-gray-500">등록된 이미지가 없습니다.</p>
        ) : (
          images.map((url) => (
            <div key={url} className="rounded-lg border border-gray-200 p-3">
              <img
                src={url}
                alt="캠핑장 이미지"
                className="h-40 w-full rounded-md object-cover"
              />
              <p className="mt-2 truncate text-xs text-gray-500">{url}</p>
            </div>
          ))
        )}
      </div>
    </section>
  );
}