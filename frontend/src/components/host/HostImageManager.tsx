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
    <section>
      <h2>이미지 관리</h2>

      <form onSubmit={handleSubmit}>
        <input
          placeholder="이미지 URL"
          value={imageUrl}
          onChange={(e) => setImageUrl(e.target.value)}
        />

        <button type="submit">이미지 추가</button>
      </form>

      <div>
        {images.length === 0 ? (
          <p>등록된 이미지가 없습니다.</p>
        ) : (
          images.map((url) => (
            <div key={url}>
              <img src={url} alt="캠핑장 이미지" />
              <p>{url}</p>
            </div>
          ))
        )}
      </div>
    </section>
  );
}