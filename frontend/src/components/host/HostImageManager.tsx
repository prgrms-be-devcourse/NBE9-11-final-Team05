"use client";

import { ChangeEvent, useEffect, useRef, useState } from "react";
import { ImagePlus, Paperclip, Star, Trash2, X } from "lucide-react";
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

  const fileInputRef = useRef<HTMLInputElement | null>(null);

  useEffect(() => {
    setImages(initialImages);
  }, [initialImages]);

  const visibleImages = showAll ? images : images.slice(0, 6);

  function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0] ?? null;
    if (!file) return;

    if (!file.type.startsWith("image/")) {
      alert("이미지 파일만 선택할 수 있습니다.");
      event.target.value = "";
      return;
    }

    if (file.size > MAX_IMAGE_SIZE) {
      alert(`${MAX_IMAGE_SIZE_MB}MB 이하의 이미지만 선택할 수 있습니다.`);
      event.target.value = "";
      return;
    }

    setSelectedFile(file);
  }

  function handleRemoveSelectedFile() {
    setSelectedFile(null);
    setThumbnail(false);

    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  }

  async function handleUpload() {
    if (!selectedFile) {
      alert("업로드할 이미지를 선택해주세요.");
      return;
    }

    try {
      setIsUploading(true);

      await addCampingImage(campingId, selectedFile, thumbnail);
      await onChange();

      setSelectedFile(null);
      setThumbnail(false);

      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }

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
    <section className="rounded-[2rem] border border-gray-100 bg-white p-8 shadow-sm">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-gray-900">이미지 관리</h2>
          <p className="mt-1 text-sm text-gray-500">
            캠핑장 이미지를 업로드하고 대표 이미지를 관리할 수 있습니다.
          </p>
        </div>
      </div>

      <div className="mt-8 rounded-2xl border border-gray-100 bg-[#FAFAF7] p-6">
        <div className="flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between">
          <div className="flex gap-4">
            <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-white text-[#3F6F43]">
              <ImagePlus className="h-5 w-5" />
            </div>

            <div>
              <h3 className="text-sm font-bold text-gray-900">이미지 업로드</h3>
              <p className="mt-1 text-sm leading-6 text-gray-500">
                JPG, PNG 이미지를 업로드할 수 있습니다. 최대 {MAX_IMAGE_SIZE_MB}
                MB까지 지원합니다.
              </p>

              {selectedFile && (
                <div className="mt-4 flex max-w-xl items-center gap-3 rounded-xl border border-gray-200 bg-white px-4 py-3">
                  <Paperclip className="h-4 w-4 shrink-0 text-gray-400" />

                  <p className="min-w-0 flex-1 truncate text-sm font-medium text-gray-700">
                    {selectedFile.name}
                  </p>

                  <button
                    type="button"
                    onClick={handleRemoveSelectedFile}
                    className="flex h-7 w-7 items-center justify-center rounded-full text-gray-400 transition hover:bg-red-50 hover:text-red-500"
                    aria-label="선택한 이미지 취소"
                  >
                    <X className="h-4 w-4" />
                  </button>
                </div>
              )}

              <label className="mt-4 inline-flex cursor-pointer items-center gap-2 text-sm font-medium text-gray-600">
                <input
                  type="checkbox"
                  checked={thumbnail}
                  onChange={(event) => setThumbnail(event.target.checked)}
                  disabled={!selectedFile}
                  className="h-4 w-4 rounded border-gray-300 accent-[#D17A2F] disabled:cursor-not-allowed disabled:opacity-50"
                />
                대표 이미지로 설정
              </label>
            </div>
          </div>

          <div className="flex shrink-0 flex-col gap-3 sm:flex-row lg:flex-col">
            <label className="inline-flex h-11 min-w-36 cursor-pointer items-center justify-center rounded-xl border border-gray-200 bg-white px-5 text-sm font-semibold text-gray-700 transition hover:bg-gray-50">
              파일 선택
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                onChange={handleFileChange}
                className="hidden"
              />
            </label>

            <button
              type="button"
              onClick={handleUpload}
              disabled={isUploading || !selectedFile}
              className="inline-flex h-11 min-w-40 items-center justify-center rounded-xl bg-[#D17A2F] px-5 text-sm font-semibold text-white shadow-sm transition hover:bg-[#BF6C26] disabled:cursor-not-allowed disabled:opacity-50"
            >
              {isUploading ? "업로드 중..." : "이미지 업로드"}
            </button>
          </div>
        </div>
      </div>

      {images.length === 0 ? (
        <div className="mt-6 rounded-2xl border border-gray-100 bg-[#FAFAF7] py-12 text-center">
          <p className="text-sm text-gray-500">등록된 이미지가 없습니다.</p>
        </div>
      ) : (
        <>
          <div className="mt-6 grid gap-5 md:grid-cols-2 xl:grid-cols-3">
            {visibleImages.map((image) => (
              <article
                key={image.imageId}
                className="overflow-hidden rounded-2xl border border-gray-100 bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-md"
              >
                <div className="relative">
                  <img
                    src={image.imageUrl}
                    alt="캠핑장 이미지"
                    className="h-52 w-full object-cover"
                  />

                  {image.thumbnail && (
                    <span className="absolute left-3 top-3 inline-flex items-center gap-1 rounded-full bg-[#D17A2F] px-3 py-1 text-xs font-semibold text-white shadow-sm">
                      <Star className="h-3 w-3 fill-white" />
                      대표 이미지
                    </span>
                  )}
                </div>

                <div className="flex items-center justify-end border-t border-gray-100 px-4 py-3">
                  <button
                    type="button"
                    onClick={() => handleDelete(image.imageId)}
                    className="inline-flex items-center gap-1.5 rounded-lg px-3 py-2 text-sm font-medium text-red-500 transition hover:bg-red-50 hover:text-red-600"
                  >
                    <Trash2 className="h-4 w-4" />
                    삭제
                  </button>
                </div>
              </article>
            ))}
          </div>

          {images.length > 6 && (
            <div className="mt-6 flex justify-center">
              <button
                type="button"
                onClick={() => setShowAll((prev) => !prev)}
                className="rounded-xl border border-gray-200 bg-white px-5 py-3 text-sm font-medium text-gray-700 transition hover:bg-gray-50"
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