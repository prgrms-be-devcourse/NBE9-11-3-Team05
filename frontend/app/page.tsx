"use client"

import { useEffect, useState } from "react"
import { FeedCard } from "@/components/feed-card"
import { Header } from "@/components/header"
import { RankingBanner } from "@/components/ranking-banner"
import { Pagination } from "@/components/pagination"
import { Filter, SlidersHorizontal } from "lucide-react"
import { Button } from "@/components/ui/button"
import { useAuth } from "@/lib/auth-context"
import { API_ENDPOINTS, apiRequest } from "@/lib/api"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"

const MAX_DAILY_HEARTS = 5

// Top 3 Ranking Data
type Top3Animal = {
  id: string
  animalId: number
  rank: number
  name: string
  imageUrl: string
  cheerTemperature: number
  maxCheerTemperature: number
}

type FeedItem = {
  id: string
  animalId: number
  processState: "보호중" | "종료(입양)"
  imageUrl: string
  animalInfo: string
  cheerTemperature: number
  maxCheerTemperature: number
  totalHeartCount: number
  adopterDiary?: string
  comments: Array<{ id: string; author: string; text: string }>
}

type AnimalListItem = {
  animalId: number
  processState?: string
  noticeNo?: string
  noticeEdt?: string
  upKindNm?: string
  kindFullNm?: string
  age?: string
  popfile1?: string
  careNm?: string
  totalCheerCount?: number
  temperature?: number
}

const ITEMS_PER_PAGE = 10
const REGION_OPTIONS = [
  "전체",
  "서울",
  "부산",
  "대구",
  "인천",
  "광주",
  "대전",
  "울산",
  "세종",
  "경기",
  "강원",
  "충북",
  "충남",
  "전북",
  "전남",
  "경북",
  "경남",
  "제주",
]
const SPECIES_OPTIONS = ["전체", "개", "고양이", "기타"]
const STATUS_OPTIONS = ["보호중", "종료"]
type SortOption = "noticeEndDate" | "cheerCount"

const parseAnimalList = (payload: unknown): AnimalListItem[] => {
  if (!payload) return []
  if (Array.isArray(payload)) return payload as AnimalListItem[]
  if (typeof payload !== "object") return []

  const response = payload as Record<string, unknown>
  if (Array.isArray(response.content)) return response.content as AnimalListItem[]
  if (Array.isArray(response.animals)) return response.animals as AnimalListItem[]
  if (response.data && typeof response.data === "object") {
    const nested = response.data as Record<string, unknown>
    if (Array.isArray(nested.content)) return nested.content as AnimalListItem[]
    if (Array.isArray(nested.animals)) return nested.animals as AnimalListItem[]
  }
  return []
}

const mapProcessState = (value?: string): "보호중" | "종료(입양)" => {
  const normalized = (value || "").toUpperCase()
  if (normalized.includes("보호") || normalized.includes("PROTECT")) return "보호중"
  return "종료(입양)"
}

const normalizeImageUrl = (value?: string): string => {
  if (!value) return "/placeholder.svg"
  const trimmed = value.trim()
  if (!trimmed || trimmed === "null" || trimmed === "undefined") return "/placeholder.svg"
  if (trimmed.startsWith("http://openapi.animal.go.kr/")) {
    return trimmed.replace("http://openapi.animal.go.kr/", "https://openapi.animal.go.kr/")
  }
  if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("/")) {
    return trimmed
  }
  return "/placeholder.svg"
}

const mapTemperature = (value?: number): number => {
  if (typeof value !== "number" || Number.isNaN(value)) return 0
  if (value <= 1) return Math.max(0, Math.min(100, value * 100))
  return Math.max(0, Math.min(100, value))
}

const mapAnimalToFeedItem = (animal: AnimalListItem): FeedItem => {
  const breed = animal.kindFullNm || animal.upKindNm || "품종 미상"
  const age = animal.age || "나이 미상"
  const shelter = animal.careNm || "보호소 정보 없음"
  const animalId = Number(animal.animalId)
  return {
    id: String(animalId),
    animalId,
    processState: mapProcessState(animal.processState),
    imageUrl: normalizeImageUrl(animal.popfile1),
    animalInfo: `${breed} · ${age} · ${shelter}`,
    cheerTemperature: mapTemperature(animal.temperature),
    maxCheerTemperature: 100,
    totalHeartCount: typeof animal.totalCheerCount === "number" ? animal.totalCheerCount : 0,
    comments: [],
  }
}

export default function SocialFeedPage() {
  const { user } = useAuth()
  const [currentPage, setCurrentPage] = useState(1)
  const [dailyHeartsRemaining, setDailyHeartsRemaining] = useState(MAX_DAILY_HEARTS)
  const [animals, setAnimals] = useState<FeedItem[]>([])
  const [totalPages, setTotalPages] = useState(1)
  const [totalAnimalCount, setTotalAnimalCount] = useState(0)
  const [selectedRegion, setSelectedRegion] = useState("전체")
  const [selectedSpecies, setSelectedSpecies] = useState("전체")
  const [selectedStatus, setSelectedStatus] = useState("보호중")
  const [sortOption, setSortOption] = useState<SortOption>("noticeEndDate")
  const [top3Animals, setTop3Animals] = useState<Top3Animal[]>([])

  const fetchTop3Animals = async () => {
    const { data } = await apiRequest<unknown>(
      `${API_ENDPOINTS.animals}?sort=totalCheerCount,DESC&size=3`
    )
    const list = parseAnimalList(data)
    const mapped = list.map((animal, index) => ({
      id: `top${index + 1}`,
      animalId: Number(animal.animalId),
      rank: index + 1,
      name: animal.kindFullNm || animal.upKindNm || "품종 미상",
      imageUrl: normalizeImageUrl(animal.popfile1),
      cheerTemperature: mapTemperature(animal.temperature),
      maxCheerTemperature: 100,
    }))
    setTop3Animals(mapped)
  }


  const extractRemainingToday = (
    payload: { [key: string]: any } | string | number | null
  ): number | null => {
    if (typeof payload === "number") return payload
    if (typeof payload === "string") {
      const parsed = Number(payload)
      return Number.isFinite(parsed) ? parsed : null
    }
    if (!payload || typeof payload !== "object") return null
    if (typeof payload.remainingToday === "number") return payload.remainingToday
    if (typeof payload.remainingToday === "string") {
      const parsed = Number(payload.remainingToday)
      if (Number.isFinite(parsed)) return parsed
    }
    if (typeof payload.remaining === "number") return payload.remaining
    if (typeof payload.remaining === "string") {
      const parsed = Number(payload.remaining)
      if (Number.isFinite(parsed)) return parsed
    }
    if (typeof payload.remainingCheers === "number") return payload.remainingCheers
    if (typeof payload.remainingCheers === "string") {
      const parsed = Number(payload.remainingCheers)
      if (Number.isFinite(parsed)) return parsed
    }
    if (payload.data && typeof payload.data === "object") {
      return extractRemainingToday(payload.data as { [key: string]: any })
    }
    if (payload.result && typeof payload.result === "object") {
      return extractRemainingToday(payload.result as { [key: string]: any })
    }
    return null
  }

  const fetchDailyHeartsRemaining = async () => {
    if (!user) {
      setDailyHeartsRemaining(MAX_DAILY_HEARTS)
      return
    }

    const { data } = await apiRequest<{ [key: string]: any }>(API_ENDPOINTS.cheersToday)
    const remainingToday = extractRemainingToday(data)
    if (remainingToday === null) {
      return
    }

    setDailyHeartsRemaining(Math.max(0, Math.min(MAX_DAILY_HEARTS, remainingToday)))
  }

  const handleCheerSuccess = (info?: { remainingToday?: number }) => {
    if (info?.remainingToday !== undefined) {
      setDailyHeartsRemaining(Math.max(0, Math.min(MAX_DAILY_HEARTS, info.remainingToday)))
    } else {
      setDailyHeartsRemaining(prev => Math.max(0, prev - 1))
      fetchDailyHeartsRemaining()
    }
  }

  const fetchAnimals = async (page: number) => {
    const stateGroup = selectedStatus === "종료" ? 1 : 0
    const queryParams = new URLSearchParams({
      page: String(Math.max(page - 1, 0)),
      stateGroup: String(stateGroup),
    })

    if (selectedRegion !== "전체") {
      queryParams.set("region", selectedRegion)
    }

    if (selectedSpecies !== "전체") {
      queryParams.set("kind", selectedSpecies)
    }

    if (sortOption === "cheerCount") {
      queryParams.set("sort", "totalCheerCount,DESC")
    }

    const query = queryParams.toString()
    const { data, error } = await apiRequest<unknown>(`${API_ENDPOINTS.animals}?${query}`)
    if (error) {
      setAnimals([])
      setTotalAnimalCount(0)
      setTotalPages(1)
      return
    }

    const parsedAnimals = parseAnimalList(data)
    setAnimals(parsedAnimals.map(mapAnimalToFeedItem))

    if (data && typeof data === "object") {
      const response = data as Record<string, unknown>
      const pages =
        typeof response.totalPages === "number"
          ? response.totalPages
          : response.data && typeof response.data === "object" && typeof (response.data as Record<string, unknown>).totalPages === "number"
            ? ((response.data as Record<string, unknown>).totalPages as number)
            : 1
      const total =
        typeof response.totalElements === "number"
          ? response.totalElements
          : typeof response.totalAnimalCount === "number"
            ? response.totalAnimalCount
            : response.data && typeof response.data === "object" && typeof (response.data as Record<string, unknown>).totalElements === "number"
              ? ((response.data as Record<string, unknown>).totalElements as number)
              : parsedAnimals.length
      setTotalPages(Math.max(1, pages))
      setTotalAnimalCount(total)
      return
    }

    setTotalPages(1)
    setTotalAnimalCount(parsedAnimals.length)
  }

  useEffect(() => {
    fetchDailyHeartsRemaining()
  }, [user])

  useEffect(() => {
    fetchAnimals(currentPage)
  }, [currentPage, selectedRegion, selectedSpecies, selectedStatus, sortOption])

  useEffect(() => {
    setCurrentPage(1)
  }, [selectedRegion, selectedSpecies, selectedStatus, sortOption])

  useEffect(() => {
    fetchTop3Animals()
  }, [])

  const handlePageChange = (page: number) => {
    setCurrentPage(page)
    window.scrollTo({ top: 600, behavior: "smooth" })
  }

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <Header dailyHeartsRemaining={dailyHeartsRemaining} maxDailyHearts={MAX_DAILY_HEARTS} />

      {/* Top 3 Ranking Banner */}
      <RankingBanner animals={top3Animals} />

      {/* Feed Section */}
      <main className="max-w-6xl mx-auto px-4 md:px-6 py-8">
        {/* Section Header */}
        <div className="flex flex-col sm:flex-row sm:items-center gap-4 mb-6">
          <div>
            <h2 className="text-lg md:text-xl font-bold text-foreground">
              전체 보호동물
            </h2>
            <p className="text-xs md:text-sm text-muted-foreground mt-1">
              총 {totalAnimalCount}마리의 친구들이 가족을 기다리고 있어요
            </p>
          </div>
          <div className="w-full sm:w-auto sm:ml-auto flex flex-col sm:flex-row gap-2 sm:gap-3 sm:justify-end">
            <div className="flex items-center gap-2 rounded-xl border border-border bg-card px-3 py-2">
              <Filter className="w-4 h-4 text-muted-foreground shrink-0" />
              <div className="grid grid-cols-1 md:grid-cols-3 gap-2 w-full sm:w-auto">
                <div className="flex items-center gap-1.5">
                  <span className="text-xs font-medium text-muted-foreground shrink-0">지역</span>
                  <Select value={selectedRegion} onValueChange={setSelectedRegion}>
                    <SelectTrigger className="h-9 rounded-lg border-border bg-background px-2">
                      <SelectValue placeholder="지역" />
                    </SelectTrigger>
                    <SelectContent>
                      {REGION_OPTIONS.map((region) => (
                        <SelectItem key={region} value={region}>
                          {region}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="flex items-center gap-1.5">
                  <span className="text-xs font-medium text-muted-foreground shrink-0">종</span>
                  <Select value={selectedSpecies} onValueChange={setSelectedSpecies}>
                    <SelectTrigger className="h-9 rounded-lg border-border bg-background px-2">
                      <SelectValue placeholder="축종" />
                    </SelectTrigger>
                    <SelectContent>
                      {SPECIES_OPTIONS.map((species) => (
                        <SelectItem key={species} value={species}>
                          {species}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="flex items-center gap-1.5">
                  <span className="text-xs font-medium text-muted-foreground shrink-0">보호상태</span>
                  <Select value={selectedStatus} onValueChange={setSelectedStatus}>
                    <SelectTrigger className="h-9 rounded-lg border-border bg-background px-2">
                      <SelectValue placeholder="상태" />
                    </SelectTrigger>
                    <SelectContent>
                      {STATUS_OPTIONS.map((status) => (
                        <SelectItem key={status} value={status}>
                          {status}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </div>

            <div className="flex items-center gap-2 rounded-xl border border-border bg-card px-2 py-2 sm:w-[220px]">
              <SlidersHorizontal className="w-4 h-4 text-muted-foreground shrink-0" />
              <div className="w-full min-w-0">
                <Select
                  value={sortOption}
                  onValueChange={(value) => setSortOption(value as SortOption)}
                >
                  <SelectTrigger className="h-9 rounded-lg border-border bg-background px-2">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="noticeEndDate">공고 종료일순</SelectItem>
                    <SelectItem value="cheerCount">응원 수 기준</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>
        </div>

        {/* Feed Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {animals.map((item) => (
            <FeedCard
              key={item.id}
              id={item.id}
              animalId={item.animalId}
              processState={item.processState}
              imageUrl={item.imageUrl}
              animalInfo={item.animalInfo}
              cheerTemperature={item.cheerTemperature}
              maxCheerTemperature={item.maxCheerTemperature}
              totalHeartCount={item.totalHeartCount}
              adopterDiary={item.adopterDiary}
              comments={item.comments}
              dailyHeartsRemaining={dailyHeartsRemaining}
              onCheerSuccess={handleCheerSuccess}
            />
          ))}
        </div>

        {/* Pagination */}
        <div className="py-10">
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={handlePageChange}
          />
          <p className="text-center text-sm text-muted-foreground mt-4">
            {currentPage} / {totalPages} 페이지
          </p>
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-card border-t border-border">
        <div className="max-w-6xl mx-auto px-4 md:px-6 py-8">
          <div className="flex flex-col md:flex-row justify-between items-center gap-4">
            <p className="text-sm text-muted-foreground">
              2024 유기동물 응원 피드. 모든 생명은 소중합니다.
            </p>
            <div className="flex items-center gap-6 text-sm text-muted-foreground">
              <a href="#" className="hover:text-foreground transition-colors">이용약관</a>
              <a href="#" className="hover:text-foreground transition-colors">개인정보처리방침</a>
              <a href="#" className="hover:text-foreground transition-colors">문의하기</a>
            </div>
          </div>
        </div>
      </footer>
    </div>
  )
}
