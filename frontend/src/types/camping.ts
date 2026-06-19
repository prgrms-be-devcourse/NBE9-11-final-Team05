import { Site } from "./site";

export interface Camping {
    id: number;
    name: string;
    address: string;
    firstImageUrl: string;
  }

  export interface CampingDetail {
    id: number;
    firstImageUrl: string | null;
    name: string;
    homepage: string;
    address: string;
    rating: number;
    description: string;
    phone: string;
    checkInTime: string;
    checkOutTime: string;
    notice: string;
    imageUrls: string[];
    sites: Site[];
  }