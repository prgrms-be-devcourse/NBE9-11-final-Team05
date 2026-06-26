"use client";

import { ChangeEvent, useEffect, useState } from "react";
import { addCampingImage, deleteCampingImage } from "@/lib/api/host";
import type { CampingImage } from "@/types/host";

interface HostImageManagerProps {
  campingId: number;
  initialImages: CampingImage[];
  onChange: () => Promise<void>;
}

const MAX_IMAGE_SIZE_MB = 5;
const MAX_IMAGE_SIZE = MAX_IMAGE_SIZE_MB * 1024 * 1024;

export default function HostImageManager({
  campingId,
  initialImages,
  onChange,
}: HostImageManagerProps) {
  const [images, setImages] = useState<CampingImage[]>(initialImages);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [thumbnail, setThumbnail] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [showAll, setShowAll] = useState(false);

  useEffect(() => {
    setImages(initialImages);
  }, [initialImages]);

  const visibleImages = showAll ? images : images.slice(0, 6);

  function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0] ?? null;
    setSelectedFile(file);
  }

  async function handleUpload() {
    if (!selectedFile) {
      alert("업로드할 이미지를 선택해주세요.");
      return;
    }

    if (!selectedFile.type.startsWith("image/")) {
      alert("이미지 파일만 업로드할 수 있습니다.");
      return;
    }

    if (selectedFile.size > MAX_IMAGE_SIZE) {
      alert(`${MAX_IMAGE_SIZE_MB}MB 이하의 이미지만 업로드할 수 있습니다.`);
      return;
    }

    try {
      setIsUploading(true);

      await addCampingImage(campingId, selectedFile, thumbnail);
      await onChange();

      setSelectedFile(null);
      setThumbnail(false);

      alert("이미지가 등록되었습니다.");
    } catch (error) {
      alert(error instanceof Error ? error.message : "이미지 등록에 실패했습니다.");
    } finally {
      setIsUploading(false);
    }
  }

  async function handleDelete(imageId: number) {
    if (!confirm("이미지를 삭제하시겠습니까?")) return;

    try {
      await deleteCampingImage(campingId, imageId);
      await onChange();
    } catch {
      alert("이미지 삭제에 실패했습니다.");
    }
  }

  return (
    <section className="space-y-8 rounded-[2rem] border border-gray-100 bg-white p-8 shadow-sm">
      <div>
        <h2 className="text-3xl font-bold text-gray-900">이미지 관리</h2>
        <p className="mt-2 text-base text-gray-600">
          캠핑장 이미지를 업로드하고 대표 이미지를 관리할 수 있습니다.
        </p>
      </div>

      <div className="rounded-2xl border border-dashed border-gray-200 bg-gray-50 p-8">
        <div className="grid gap-8 md:grid-cols-[1fr_180px] md:items-center">
          <div>
            <h3 className="text-lg font-semibold text-gray-900">
              이미지 업로드
            </h3>

            <p className="mt-2 max-w-xl text-sm leading-6 text-gray-600">
              JPG, PNG 등의 이미지를 업로드할 수 있습니다.
              <br />
              최대 {MAX_IMAGE_SIZE_MB}MB까지 지원합니다.
            </p>

            {selectedFile && (
              <div className="mt-5 rounded-xl border border-gray-200 bg-white px-4 py-3">
                <p className="truncate text-sm font-medium text-gray-700">
                  📎 {selectedFile.name}
                </p>
              </div>
            )}

            <label className="mt-5 inline-flex cursor-pointer items-center gap-3 text-sm font-medium text-gray-700">
              <input
                type="checkbox"
                checked={thumbnail}
                onChange={(event) => setThumbnail(event.target.checked)}
                className="h-4 w-4 rounded border-gray-300 accent-[#d97c29]"
              />
              대표 이미지로 설정
            </label>
          </div>

          <div className="flex flex-col gap-4">
            <label className="flex h-12 cursor-pointer items-center justify-center rounded-xl border border-gray-300 bg-white text-sm font-semibold text-gray-700 transition hover:bg-gray-100">
              파일 선택
              <input
                type="file"
                accept="image/*"
                onChange={handleFileChange}
                className="hidden"
              />
            </label>

            <button
              type="button"
              onClick={handleUpload}
              disabled={isUploading}
              className="flex h-12 items-center justify-center rounded-xl bg-[#d97c29] text-sm font-semibold text-white shadow-sm transition hover:bg-[#c96f22] disabled:cursor-not-allowed disabled:opacity-50"
            >
              {isUploading ? "업로드 중..." : "이미지 업로드"}
            </button>
          </div>
        </div>
      </div>

      {images.length === 0 ? (
        <div className="rounded-2xl border border-dashed border-gray-200 py-16 text-center">
          <p className="text-gray-500">등록된 이미지가 없습니다.</p>
        </div>
      ) : (
        <>
          <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
            {visibleImages.map((image) => (
              <div
                key={image.imageId}
                className="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm"
              >
                <div className="relative">
                  <img
                    src={image.imageUrl}
                    alt="캠핑장 이미지"
                    className="h-56 w-full object-cover"
                  />

                  {image.thumbnail && (
                    <span className="absolute left-3 top-3 rounded-full bg-[#d97c29] px-3 py-1 text-xs font-semibold text-white">
                      대표 이미지
                    </span>
                  )}
                </div>

                <div className="flex items-center justify-between p-4">
                  <p className="mr-4 flex-1 truncate text-xs text-gray-500">
                    {image.imageUrl}
                  </p>

                  <button
                    type="button"
                    onClick={() => handleDelete(image.imageId)}
                    className="rounded-lg px-3 py-2 text-sm font-medium text-red-500 transition hover:bg-red-50 hover:text-red-600"
                  >
                    삭제
                  </button>
                </div>
              </div>
            ))}
          </div>

          {images.length > 6 && (
            <div className="flex justify-center">
              <button
                type="button"
                onClick={() => setShowAll((prev) => !prev)}
                className="rounded-xl border border-gray-300 px-5 py-3 text-sm font-medium text-gray-700 transition hover:bg-gray-100"
              >
                {showAll ? "접기" : `이미지 더보기 (${images.length - 6}장)`}
              </button>
            </div>
          )}
        </>
      )}
    </section>
  );
}