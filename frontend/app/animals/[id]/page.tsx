"use client"

import { useState, useEffect, use } from "react"
import { ImageWithFallback } from "@/components/ui/image-with-fallback"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { ArrowLeft, Heart, Phone, MapPin, Calendar, Info, User as UserIcon, Send, MessageCircle, Edit2, Trash2, PawPrint } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Header } from "@/components/header"
import { useAuth } from "@/lib/auth-context"
import { cn } from "@/lib/utils"
import {
  API_ENDPOINTS,
  getNameCandidates,
  apiRequest,
  normalizeAnimalTemperatureDisplay,
  parseAddCheerResponse,
  proposeName,
  voteName,
  type Animal,
  type Comment,
  type NamingCandidatesResponse,
} from "@/lib/api"

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

const normalizeAnimalDetail = (payload: unknown): Animal | null => {
  if (!payload) return null
  if (typeof payload !== "object") return null

  const response = payload as Record<string, unknown>
  const rawAnimal =
    response.animal && typeof response.animal === "object"
      ? (response.animal as Record<string, unknown>)
      : response.data && typeof response.data === "object"
        ? ((response.data as Record<string, unknown>).animal as Record<string, unknown> | undefined) ||
          (response.data as Record<string, unknown>)
        : response

  const animalId = Number(rawAnimal.animalId)
  if (!Number.isFinite(animalId)) return null
  const rawSex = String(rawAnimal.sexCd ?? rawAnimal.gender ?? "")
  const mappedGender = rawSex === "W" ? "F" : rawSex
  const rawTemp = Number(rawAnimal.temperature ?? 0)
  const mappedTemperature = Number.isFinite(rawTemp)
    ? (rawTemp <= 1 ? rawTemp * 100 : rawTemp)
    : 0

    const rawShelterId = rawAnimal.careRegNo ?? 
      rawAnimal.shelterId ?? 
      rawAnimal.care_reg_no ?? 
      rawAnimal.shelter_id ?? 
      (typeof rawAnimal.shelter === "object" && rawAnimal.shelter !== null 
        ? ((rawAnimal.shelter as any).shelterId ?? (rawAnimal.shelter as any).id ?? (rawAnimal.shelter as any).careRegNo)
        : undefined);
    
    return {
      animalId,
      animalName: typeof rawAnimal.animalName === "string" ? rawAnimal.animalName : null,
      stateGroup: Number(rawAnimal.stateGroup ?? (String(rawAnimal.processState ?? "").includes("보호") ? 0 : 1)),
      noticeNo: String(rawAnimal.noticeNo ?? ""),
      kind: String(rawAnimal.upKindNm ?? rawAnimal.kind ?? ""),
      breed: String(rawAnimal.kindFullNm ?? rawAnimal.breed ?? ""),
      age: String(rawAnimal.age ?? ""),
      gender: mappedGender,
      neutered: String(rawAnimal.neutered ?? ""),
      weight: String(rawAnimal.weight ?? ""),
      color: String(rawAnimal.colorCd ?? rawAnimal.color ?? ""),
      specialMark: String(rawAnimal.specialMark ?? ""),
      imageUrl: normalizeImageUrl(String(rawAnimal.popfile1 ?? rawAnimal.imageUrl ?? "")),
      shelterName: String(rawAnimal.careNm ?? rawAnimal.shelterName ?? ""),
      shelterTel: String(rawAnimal.careTel ?? rawAnimal.shelterTel ?? ""),
      shelterAddr: String(rawAnimal.careAddr ?? rawAnimal.shelterAddr ?? ""),
      chargeNm: String(rawAnimal.chargeNm ?? ""),
      region: String(rawAnimal.region ?? ""),
      noticeStartDate: String(rawAnimal.noticeStartDate ?? rawAnimal.noticeSdt ?? ""),
      noticeEndDate: String(rawAnimal.noticeEndDate ?? rawAnimal.noticeEdt ?? ""),
      processState: String(rawAnimal.processState ?? ""),
      heartCount: Number(rawAnimal.totalCheerCount ?? rawAnimal.heartCount ?? 0) || 0,
      temperature: mappedTemperature,
      shelterId: rawShelterId ? String(rawShelterId) : undefined,
    }
  }

export default function AnimalDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const resolvedParams = use(params)
  const router = useRouter()
  const { user } = useAuth()
  const [animal, setAnimal] = useState<Animal | null>(null)
  const [comments, setComments] = useState<any[]>([])
  const [newComment, setNewComment] = useState("")
  const [isSubmittingComment, setIsSubmittingComment] = useState(false)
  const [editingCommentId, setEditingCommentId] = useState<number | null>(null)
  const [editContent, setEditContent] = useState("")
  const [totalHearts, setTotalHearts] = useState(0)
  const [currentTemp, setCurrentTemp] = useState(0)
  const [remainingToday, setRemainingToday] = useState<number | null>(null)
  const [isAnimating, setIsAnimating] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [isAdoptionDialogOpen, setIsAdoptionDialogOpen] = useState(false)
  const [applyReason, setApplyReason] = useState("")
  const [applyTel, setApplyTel] = useState("")
  const [isSubmittingApplication, setIsSubmittingApplication] = useState(false)
  const [nameCandidates, setNameCandidates] = useState<NamingCandidatesResponse | null>(null)
  const [nameProposal, setNameProposal] = useState("")
  const [isNamingLoading, setIsNamingLoading] = useState(false)
  const [isSubmittingName, setIsSubmittingName] = useState(false)
  const [votingCandidateId, setVotingCandidateId] = useState<number | null>(null)
  const [showAllCandidates, setShowAllCandidates] = useState(false)

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

  const fetchRemainingToday = async () => {
    if (!user) {
      setRemainingToday(5)
      return
    }

    const { data } = await apiRequest<{ [key: string]: any }>(API_ENDPOINTS.cheersToday)
    const parsedRemaining = extractRemainingToday(data)
    if (parsedRemaining === null) {
      return
    }

    setRemainingToday(Math.max(0, Math.min(5, parsedRemaining)))
  }

  const fetchNameCandidates = async (animalId: number) => {
    setIsNamingLoading(true)
    const { data } = await getNameCandidates(animalId)
    setNameCandidates(data)
    setIsNamingLoading(false)
  }

  useEffect(() => {
    const fetchAnimalDetail = async () => {
      setIsLoading(true)
      const { data } = await apiRequest<unknown>(API_ENDPOINTS.animalDetail(Number(resolvedParams.id)))
      const normalizedAnimal = normalizeAnimalDetail(data)

      if (normalizedAnimal) {
        setAnimal(normalizedAnimal)
        setTotalHearts(normalizedAnimal.heartCount)
        setCurrentTemp(normalizedAnimal.temperature)
        await fetchNameCandidates(normalizedAnimal.animalId)
      } else {
        setAnimal(null)
        setNameCandidates(null)
      }

      await fetchComments()
      await fetchRemainingToday()
      setIsLoading(false)
    }

    fetchAnimalDetail()
  }, [resolvedParams.id, user])

  const handleCheer = async () => {
    if (!user) {
      alert("로그인이 필요합니다")
      return
    }

    if (!animal) return
    if (remainingToday !== null && remainingToday <= 0) {
      alert("오늘 사용할 수 있는 하트를 모두 사용했습니다. 내일 다시 응원해주세요!")
      return
    }

    const { data, error } = await apiRequest<unknown>(API_ENDPOINTS.addCheer(animal.animalId), {
      method: "POST",
    })
    if (error) {
      alert(error)
      return
    }

    const cheer = parseAddCheerResponse(data)
    if (!cheer) {
      console.warn("addCheer: unexpected response body", data)
      return
    }

    setTotalHearts(cheer.cheerCount)
    setCurrentTemp(normalizeAnimalTemperatureDisplay(cheer.temperature, 100))
    setRemainingToday(Math.max(0, Math.min(5, cheer.remaingCheersToday)))
    setAnimal((prev) =>
      prev
        ? {
            ...prev,
            heartCount: cheer.cheerCount,
            temperature: cheer.temperature,
          }
        : prev
    )
    setIsAnimating(true)
    setTimeout(() => setIsAnimating(false), 300)
  }

  const handleOpenAdoptionDialog = () => {
    if (!user) {
      alert("로그인이 필요합니다")
      return
    }

    setIsAdoptionDialogOpen(true)
  }

  const handleSubmitAdoptionApplication = async () => {
    if (!animal) return

    if (!applyReason.trim()) {
      alert("입양 신청 사유를 입력해주세요.")
      return
    }

    if (!applyTel.trim()) {
      alert("연락처를 입력해주세요.")
      return
    }

    setIsSubmittingApplication(true)

    const { error } = await apiRequest(API_ENDPOINTS.applyAdoption(animal.animalId), {
      method: "POST",
      body: JSON.stringify({
        applyReason,
        applyTel,
      }),
    })

    setIsSubmittingApplication(false)

    if (error) {
      alert(error)
      return
    }

    alert("입양 신청이 접수되었습니다.")
    setIsAdoptionDialogOpen(false)
    setApplyReason("")
    setApplyTel("")
  }

  const handleNameProposalChange = (value: string) => {
    const hangulOnly = value.replace(/[^가-힣ㄱ-ㅎㅏ-ㅣ]/g, "")
    setNameProposal(hangulOnly.slice(0, 10))
  }

  const handleProposeName = async () => {
    if (!animal) return
    if (!user) {
      router.push("/login")
      return
    }
    if (!nameProposal.trim()) {
      alert("제안할 이름을 입력해주세요.")
      return
    }

    setIsSubmittingName(true)
    const { error } = await proposeName(animal.animalId, { proposedName: nameProposal.trim() })
    setIsSubmittingName(false)

    if (error) {
      alert(error)
      return
    }

    setNameProposal("")
    await fetchNameCandidates(animal.animalId)
  }

  const handleVoteName = async (candidateId: number) => {
    if (!animal) return
    if (!user) {
      router.push("/login")
      return
    }

    setVotingCandidateId(candidateId)
    const { error } = await voteName(candidateId)
    setVotingCandidateId(null)

    if (error) {
      alert(error)
      return
    }

    await fetchNameCandidates(animal.animalId)
  }

  const fetchComments = async () => {
    const { data } = await apiRequest<{ comments: any[] }>(API_ENDPOINTS.comments(Number(resolvedParams.id)))
    if (data?.comments) {
      // Sort older comments first or newer first based on preference. Default to newest at bottom.
      setComments(data.comments.sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()))
    } else {
      setComments([])
    }
  }

  const handleCommentSubmit = async () => {
    if (!user) {
      alert("로그인이 필요합니다")
      return
    }
    if (!newComment.trim() || isSubmittingComment) return

    setIsSubmittingComment(true)
    const { error } = await apiRequest(API_ENDPOINTS.comments(Number(resolvedParams.id)), {
      method: "POST",
      body: JSON.stringify({ content: newComment })
    })
    setIsSubmittingComment(false)

    if (error) {
      alert("댓글 작성에 실패했습니다.")
      return
    }

    setNewComment("")
    fetchComments()
  }

  const handleCommentDelete = async (commentId: number) => {
    if (!confirm("댓글을 정말 삭제하시겠습니까?")) return
    const { error } = await apiRequest(API_ENDPOINTS.deleteComment(Number(resolvedParams.id), commentId), {
      method: "DELETE"
    })
    if (error) {
      alert("댓글 삭제에 실패했습니다.")
    } else {
      fetchComments()
    }
  }

  const handleCommentEditSubmit = async (commentId: number) => {
    if (!editContent.trim()) return
    const { error } = await apiRequest(API_ENDPOINTS.updateComment(Number(resolvedParams.id), commentId), {
      method: "PATCH",
      body: JSON.stringify({ content: editContent })
    })
    if (error) {
      alert("댓글 수정에 실패했습니다.")
    } else {
      setEditingCommentId(null)
      fetchComments()
    }
  }

  if (isLoading) {
    return (
      <div className="min-h-screen bg-background">
        <Header />
        <div className="flex items-center justify-center py-20">
          <div className="text-muted-foreground">로딩중...</div>
        </div>
      </div>
    )
  }

  if (!animal) {
    return (
      <div className="min-h-screen bg-background">
        <Header />
        <div className="flex items-center justify-center py-20">
          <div className="text-muted-foreground">동물 정보를 불러오지 못했습니다.</div>
        </div>
      </div>
    )
  }

  const isClosedAnimal = animal.stateGroup === 1
  const isProtecting = !isClosedAnimal && animal.processState === "보호중"
  const displayAnimalName = nameCandidates?.animalName ?? animal.animalName ?? null
  const sortedNameCandidates = [...(nameCandidates?.candidateDtoList ?? [])].sort(
    (left, right) => right.voteCount - left.voteCount
  )
  const hasVotedName = sortedNameCandidates.some((candidate) => candidate.isVoted)
  const genderLabel = animal.gender === "M" ? "수컷" : animal.gender === "F" ? "암컷" : "미상"
  const neuteredLabel = animal.neutered === "Y" ? "중성화 O" : animal.neutered === "N" ? "중성화 X" : "미상"
  const addressText = animal.shelterAddr || animal.region || "주소 정보 없음"
  const shelterDisplayName = animal.shelterName || "보호소 정보 없음"
  const shelterDisplayTel = animal.shelterTel || "연락처 정보 없음"
  const shelterDisplayAddress = animal.shelterAddr || "주소 정보 없음"
  const namingSection = (
    <Card className="h-full border-0 bg-card shadow-sm lg:min-h-[286px]">
      <CardContent className="flex h-full flex-col p-5 space-y-4">
        <div className="rounded-xl bg-primary/5 px-4 py-3">
          <p className="text-base font-bold text-foreground">
            {displayAnimalName
              ? `안녕, 내 이름은 ${displayAnimalName}이야!`
              : "아직 이름이 없어요. 이름을 지어주세요!"}
          </p>
          {isClosedAnimal && (
            <p className="mt-1 text-xs text-muted-foreground">보호가 종료되어 이름 제안과 투표가 마감되었습니다.</p>
          )}
        </div>

        {!displayAnimalName && (
          <div className="flex gap-2">
            <Input
              value={nameProposal}
              onChange={(event) => handleNameProposalChange(event.target.value)}
              placeholder="한글 10자 이내"
              maxLength={10}
              disabled={isClosedAnimal || isSubmittingName}
              className="h-10 rounded-xl bg-secondary/50 border-0"
            />
            <Button
              onClick={handleProposeName}
              disabled={isClosedAnimal || isSubmittingName || !nameProposal.trim()}
              className="h-10 shrink-0 rounded-xl bg-primary px-3 text-primary-foreground hover:bg-primary/90"
            >
              {isSubmittingName ? "제안 중" : "제안"}
            </Button>
          </div>
        )}

        <div className="flex flex-col space-y-2">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-semibold text-foreground">이름 후보</h3>
            <span className="text-xs text-muted-foreground">{nameCandidates?.totalCandidates ?? 0}개</span>
          </div>

          {isNamingLoading ? (
            <div className="py-5 text-center text-xs text-muted-foreground">후보를 불러오는 중...</div>
          ) : sortedNameCandidates.length === 0 ? (
            <div className="py-5 text-center text-xs text-muted-foreground">아직 제안된 이름이 없습니다.</div>
          ) : (
            <div className="grid gap-2">
              {(showAllCandidates ? sortedNameCandidates : sortedNameCandidates.slice(0, 3)).map((candidate) => {
                const isVoteDone = candidate.isVoted
                return (
                  <div
                    key={candidate.candidateId}
                    className={`rounded-xl border p-3 ${
                      isVoteDone ? "border-primary bg-primary/5" : "border-border bg-background"
                    }`}
                  >
                    <div className="flex items-center justify-between gap-2">
                      <div className="min-w-0">
                        <p className="text-sm font-semibold text-foreground">{candidate.proposedName}</p>
                        <p className="mt-0.5 text-[11px] text-muted-foreground">
                          {candidate.proposerNickname || "-"} · {candidate.voteCount}표
                        </p>
                      </div>
                      <Button
                        size="sm"
                        variant={isVoteDone ? "default" : "outline"}
                        onClick={() => !user ? router.push("/login") : handleVoteName(candidate.candidateId)}
                        disabled={isClosedAnimal || votingCandidateId === candidate.candidateId || (hasVotedName && !isVoteDone)}
                        className={`h-8 shrink-0 rounded-xl px-3 text-xs ${isVoteDone ? "bg-primary text-primary-foreground" : ""}`}
                      >
                        {isVoteDone
                          ? "투표 완료"
                          : !user
                            ? "로그인"
                            : votingCandidateId === candidate.candidateId
                              ? "투표 중"
                              : "투표"}
                      </Button>
                    </div>
                  </div>
                )
              })}
              {!showAllCandidates && sortedNameCandidates.length > 3 && (
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => setShowAllCandidates(true)}
                  className="w-full text-xs text-muted-foreground hover:text-primary mt-1"
                >
                  모두 보기 ({sortedNameCandidates.length})
                </Button>
              )}
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )

  return (
    <div className="min-h-screen bg-background">
      <Header dailyHeartsRemaining={remainingToday ?? 5} maxDailyHearts={5} />

      <main className="max-w-4xl mx-auto px-4 py-6">
        {/* Back Button */}
        <Link href="/" className="inline-flex items-center gap-2 text-muted-foreground hover:text-foreground transition-colors mb-6">
          <ArrowLeft className="w-4 h-4" />
          <span className="text-sm font-medium">목록으로</span>
        </Link>

        <div className="grid grid-cols-1 items-stretch gap-8 lg:grid-cols-2">
          {/* Image Section */}
          <div className="flex h-full flex-col space-y-4">
            <div className="relative aspect-square rounded-2xl overflow-hidden bg-secondary">
              <ImageWithFallback
                src={animal.imageUrl}
                alt={`${animal.breed} - ${animal.noticeNo}`}
                fill
                className="object-cover"
                priority
                unoptimized
              />
              {!isProtecting && (
                <div className="absolute inset-0 flex items-center justify-center bg-foreground/20">
                  <div className="bg-success/90 text-success-foreground px-8 py-4 rounded-xl rotate-[-12deg] shadow-lg">
                    <span className="text-xl font-bold tracking-wide">입양완료</span>
                  </div>
                </div>
              )}
            </div>

            {/* Status Badge */}
            <div className="flex items-center gap-2">
              {isProtecting ? (
                <span className="inline-flex items-center gap-1.5 px-4 py-2 rounded-full bg-primary/10 text-primary text-sm font-semibold">
                  보호중
                </span>
              ) : (
                <span className="inline-flex items-center gap-1.5 px-4 py-2 rounded-full bg-success/10 text-success text-sm font-semibold">
                  입양 완료
                </span>
              )}
              <span className="text-sm text-muted-foreground">
                공고번호: {animal.noticeNo}
              </span>
            </div>

            <div className="flex flex-1 flex-col">
              {namingSection}
            </div>
          </div>

          {/* Info Section */}
          <div className="flex h-full flex-col space-y-6">
            {/* Basic Info */}
            <div>
              <h1 className="text-2xl font-bold text-foreground mb-2">
                {animal.breed}
              </h1>
              <p className="text-muted-foreground">
                {animal.kind} / {animal.age} / {genderLabel}
              </p>
            </div>

            {/* Details Grid */}
            <Card className="border-0 bg-secondary/30">
              <CardContent className="space-y-5 p-5">
                <div className="grid grid-cols-2 gap-x-4 gap-y-4">
                  <div className="space-y-1">
                    <p className="text-xs text-muted-foreground">체중</p>
                    <p className="text-sm font-medium text-foreground">{animal.weight}kg</p>
                  </div>
                  <div className="space-y-1">
                    <p className="text-xs text-muted-foreground">색상</p>
                    <p className="text-sm font-medium text-foreground">{animal.color}</p>
                  </div>
                  <div className="space-y-1">
                    <p className="text-xs text-muted-foreground">중성화</p>
                    <p className="text-sm font-medium text-foreground">{neuteredLabel}</p>
                  </div>
                  <div className="space-y-1">
                    <p className="text-xs text-muted-foreground">주소</p>
                    <p className="text-sm font-medium text-foreground break-words">{addressText}</p>
                  </div>
                </div>

                <div className="border-t border-border/60 pt-4">
                  <h3 className="mb-3 text-sm font-semibold text-foreground">보호소 정보</h3>
                  <div className="space-y-3">
                    <div className="flex items-start gap-3">
                      <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-background/70">
                        <Info className="h-4 w-4 text-muted-foreground" />
                      </div>
                      <div className="min-w-0 pt-0.5">
                        <p className="text-xs text-muted-foreground">담당</p>
                        <p className="text-sm font-medium text-foreground">{shelterDisplayName}</p>
                      </div>
                    </div>
                    <div className="flex items-start gap-3">
                      <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-background/70">
                        <Phone className="h-4 w-4 text-muted-foreground" />
                      </div>
                      <div className="min-w-0 pt-0.5">
                        <p className="text-xs text-muted-foreground">연락처</p>
                        {animal.shelterTel ? (
                          <a href={`tel:${animal.shelterTel}`} className="text-sm font-medium text-primary hover:underline">
                            {shelterDisplayTel}
                          </a>
                        ) : (
                          <p className="text-sm font-medium text-foreground">{shelterDisplayTel}</p>
                        )}
                      </div>
                    </div>
                    <div className="flex items-start gap-3">
                      <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-background/70">
                        <MapPin className="h-4 w-4 text-muted-foreground" />
                      </div>
                      <div className="min-w-0 pt-0.5">
                        <p className="text-xs text-muted-foreground">주소</p>
                        <p className="text-sm font-medium leading-6 text-foreground break-words">{shelterDisplayAddress}</p>
                      </div>
                    </div>
                  </div>
                </div>

                {animal.shelterId && (
                  <div className="pt-2">
                    <Link href={`/shelter/${animal.shelterId}`}>
                      <Button variant="outline" className="w-full h-11 rounded-xl bg-background border-primary/20 hover:bg-primary/5 hover:border-primary/40 text-primary transition-all group">
                        <Heart className="mr-2 h-4 w-4 fill-current group-hover:scale-110 transition-transform" />
                        보호소 후원하기 & 알아보기
                      </Button>
                    </Link>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Special Mark */}
            {animal.specialMark && (
              <div className="flex items-start gap-3 p-4 rounded-xl bg-accent/30 border border-accent/50">
                <Info className="w-5 h-5 text-accent-foreground shrink-0 mt-0.5" />
                <div>
                  <p className="text-sm font-medium text-foreground">특이사항</p>
                  <p className="text-sm text-muted-foreground mt-1">{animal.specialMark}</p>
                </div>
              </div>
            )}

            {/* Notice Period */}
            <div className="flex items-center gap-3 text-sm">
              <Calendar className="w-4 h-4 text-muted-foreground" />
              <span className="text-muted-foreground">
                공고기간: {animal.noticeStartDate} ~ {animal.noticeEndDate}
              </span>
            </div>

            {/* Heart/Cheer Section */}
            {isProtecting && (
              <Card className="mt-auto border-0 bg-primary/5 lg:min-h-[286px]">
                <CardContent className="flex h-full flex-col justify-between gap-4 p-5">
                  {/* Cheer Button - Full Width */}
                  <button
                    onClick={handleCheer}
                    disabled={remainingToday !== null && remainingToday <= 0}
                    className="flex h-11 w-full items-center justify-center gap-2 rounded-xl px-4 transition-all bg-primary/10 hover:bg-primary/20 text-primary disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:bg-primary/10"
                  >
                    <Heart className={cn(
                      "w-5 h-5 transition-transform",
                      isAnimating && "scale-125 fill-primary"
                    )} />
                    <span className="font-medium">응원하기</span>
                  </button>

                  {/* Temperature Bar */}
                  <div className="space-y-2.5">
                    <div className="flex items-center justify-between text-sm">
                      <span className="font-medium text-foreground">응원 온도</span>
                      <span className="text-primary font-semibold">{currentTemp.toFixed(1)}C / 100C</span>
                    </div>
                    <div className="h-3 bg-secondary rounded-full overflow-hidden">
                      <div
                        className="h-full bg-gradient-to-r from-primary to-accent rounded-full transition-all duration-500"
                        style={{ width: `${Math.min(currentTemp, 100)}%` }}
                      />
                    </div>
                    <p className="text-sm text-muted-foreground text-center">
                      {totalHearts}명이 응원했어요
                    </p>
                    {remainingToday !== null && (
                      <p className="text-xs text-muted-foreground text-center">
                        오늘 남은 하트 {remainingToday}개
                      </p>
                    )}
                  </div>

                  <div className="rounded-xl border border-primary/15 bg-background/80 p-3 shadow-sm">
                    <div className="flex items-center justify-between gap-3">
                      <div className="min-w-0">
                        <p className="text-sm font-semibold text-foreground">입양을 고민 중이신가요?</p>
                        <p className="mt-1 text-xs text-muted-foreground">
                          동물 상세를 확인하셨다면 바로 신청서를 작성해보세요.
                        </p>
                      </div>
                      <Button
                        type="button"
                        onClick={handleOpenAdoptionDialog}
                        className="h-10 shrink-0 rounded-xl bg-primary px-4 text-primary-foreground shadow-sm hover:bg-primary/90"
                      >
                        <PawPrint className="mr-2 h-4 w-4" />
                        입양 신청
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            )}
          </div>
        </div>

        {/* Comments Section */}
        <Card className="mt-8 border-0 bg-card shadow-sm">
          <CardContent className="p-6">
            <div className="flex items-center gap-2 mb-4">
              <MessageCircle className="w-5 h-5 text-muted-foreground" />
              <h3 className="font-semibold text-foreground">응원 댓글</h3>
              <span className="text-sm text-muted-foreground">({comments.length})</span>
            </div>

            {/* Comment List */}
            <div className="space-y-4 mb-4">
              {comments.length === 0 ? (
                <p className="text-sm text-muted-foreground text-center py-8">
                  첫 번째 응원 댓글을 남겨주세요!
                </p>
              ) : (
                comments.map((comment) => (
                  <div key={comment.commentId} className="flex gap-3 group">
                    <div className="w-8 h-8 rounded-full bg-secondary flex items-center justify-center shrink-0 overflow-hidden">
                      {comment.profileImageUrl ? (
                        <ImageWithFallback 
                          src={comment.profileImageUrl} 
                          alt={comment.nickname || "User"} 
                          width={32} 
                          height={32} 
                          className="w-full h-full object-cover"
                        />
                      ) : (
                        <UserIcon className="w-4 h-4 text-muted-foreground" />
                      )}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between gap-2 overflow-hidden">
                        <div className="flex items-center gap-2 min-w-0">
                          <span className="text-sm font-semibold text-foreground truncate">{comment.nickname || "익명"}</span>
                          <span className="text-xs text-muted-foreground shrink-0">
                            {new Date(comment.createdAt).toLocaleDateString("ko-KR")}
                          </span>
                        </div>
                        {user?.id === comment.userId && (
                          <div className="flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                            <button
                              onClick={() => {
                                setEditingCommentId(comment.commentId)
                                setEditContent(comment.content)
                              }}
                              className="text-muted-foreground hover:text-primary transition-colors"
                            >
                              <Edit2 className="w-3.5 h-3.5" />
                            </button>
                            <button
                              onClick={() => handleCommentDelete(comment.commentId)}
                              className="text-muted-foreground hover:text-destructive transition-colors"
                            >
                              <Trash2 className="w-3.5 h-3.5" />
                            </button>
                          </div>
                        )}
                      </div>

                      {editingCommentId === comment.commentId ? (
                        <div className="flex gap-2 mt-2">
                          <Input
                            value={editContent}
                            onChange={(e) => setEditContent(e.target.value)}
                            onKeyDown={(e) => e.key === "Enter" && handleCommentEditSubmit(comment.commentId)}
                            autoFocus
                            className="h-8 text-sm"
                          />
                          <Button size="sm" onClick={() => handleCommentEditSubmit(comment.commentId)}>수정</Button>
                          <Button size="sm" variant="ghost" onClick={() => setEditingCommentId(null)}>취소</Button>
                        </div>
                      ) : (
                        <p className="text-sm text-muted-foreground mt-1 whitespace-pre-wrap">{comment.content}</p>
                      )}
                    </div>
                  </div>
                ))
              )}
            </div>

            {/* Comment Input */}
            <div className="flex gap-2">
              <Input
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
                placeholder={user ? "응원 댓글을 남겨주세요..." : "로그인 후 댓글을 남길 수 있습니다"}
                className="flex-1 rounded-xl bg-secondary/50 border-0 h-11"
                disabled={!user || isSubmittingComment}
                onKeyDown={(e) => {
                  if (e.key === "Enter" && !e.repeat) {
                    e.preventDefault()
                    handleCommentSubmit()
                  }
                }}
              />
              <Button
                onClick={handleCommentSubmit}
                disabled={!user || !newComment.trim() || isSubmittingComment}
                className="rounded-xl bg-primary text-primary-foreground hover:bg-primary/90"
              >
                <Send className="w-4 h-4" />
              </Button>
            </div>
          </CardContent>
        </Card>
      </main>

      <Dialog open={isAdoptionDialogOpen} onOpenChange={setIsAdoptionDialogOpen}>
        <DialogContent className="max-w-md rounded-2xl border-0 p-0 shadow-2xl">
          <div className="overflow-hidden rounded-2xl bg-background">
            <DialogHeader className="border-b border-border/60 bg-primary/5 px-6 py-5">
              <DialogTitle className="flex items-center gap-2 text-xl">
                <PawPrint className="h-5 w-5 text-primary" />
                입양 신청서 작성
              </DialogTitle>
              <DialogDescription className="pt-1 text-sm leading-6">
                {animal?.breed}에 대한 입양 신청 내용을 작성해주세요.
              </DialogDescription>
            </DialogHeader>

            <div className="space-y-5 px-6 py-5">
              <div className="rounded-2xl bg-secondary/40 p-4">
                <p className="text-xs font-medium uppercase tracking-[0.14em] text-muted-foreground">
                  신청 대상
                </p>
                <p className="mt-2 text-base font-semibold text-foreground">
                  {animal?.breed}
                </p>
                <p className="mt-1 text-sm text-muted-foreground">
                  {animal?.shelterName || "보호소 정보 없음"}
                </p>
                <div className="mt-4 border-t border-border/50 pt-4">
                  <p className="text-xs font-medium uppercase tracking-[0.14em] text-muted-foreground">
                    신청자
                  </p>
                  <p className="mt-2 text-sm font-medium text-foreground">
                    {user?.name || "이름 정보 없음"}
                  </p>
                </div>
              </div>

              <div className="space-y-2">
                <label htmlFor="applyReason" className="text-sm font-medium text-foreground">
                  신청 사유
                </label>
                <Textarea
                  id="applyReason"
                  value={applyReason}
                  onChange={(e) => setApplyReason(e.target.value)}
                  placeholder="입양을 결심한 이유와 돌봄 계획을 작성해주세요."
                  className="min-h-32 rounded-xl border-0 bg-secondary/50 px-4 py-3 shadow-none focus-visible:ring-2"
                />
              </div>

              <div className="space-y-2">
                <label htmlFor="applyTel" className="text-sm font-medium text-foreground">
                  연락처
                </label>
                <Input
                  id="applyTel"
                  value={applyTel}
                  onChange={(e) => setApplyTel(e.target.value)}
                  placeholder="예: 010-1234-5678"
                  className="h-12 rounded-xl border-0 bg-secondary/50 shadow-none"
                />
              </div>
            </div>

            <DialogFooter className="border-t border-border/60 bg-background px-6 py-4 sm:justify-between">
              <Button
                type="button"
                variant="ghost"
                className="rounded-xl"
                onClick={() => setIsAdoptionDialogOpen(false)}
              >
                닫기
              </Button>
              <Button
                type="button"
                onClick={handleSubmitAdoptionApplication}
                disabled={isSubmittingApplication}
                className="rounded-xl bg-primary px-5 text-primary-foreground hover:bg-primary/90"
              >
                {isSubmittingApplication ? "접수 중..." : "신청서 제출"}
              </Button>
            </DialogFooter>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  )
}
