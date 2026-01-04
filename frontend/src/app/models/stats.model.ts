export interface ProductStat {
    name: string;
    value: number;
}

export interface AdminStats {
    totalSoldItems: number;
    mostPopular: ProductStat[];
    mostProfitable: ProductStat[];
}

export interface UserStats {
    mostFrequentlyPurchased: ProductStat[];
    mostRecentlyPurchased: ProductStat[];
}
