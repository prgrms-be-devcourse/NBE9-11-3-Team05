"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import Link from "next/link"
import Image from "next/image"
import { Header } from "@/components/header"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { useAuth } from "@/lib/auth-context"
import { apiRequest, API_ENDPOINTS, cancelMyAdoption, getMyAdoptions } from "@/lib/api"
import { User as UserIcon, Heart, FileText, Settings, Calendar, ThermometerSun, X, MessageSquare, Phone, MapPin } from "lucide-react"
import { Input } from "@/components/ui/input"
import { formatDate } from "@/lib/utils"
import { MyFeedComment, MyAnimalComment, User, MyAdoptionApplication, AdoptionStatus } from "@/lib/api"

interface CheeredAnimal {
  animalId: number
  kind: string
  breed?: string
  imageUrl?: string
  heartCount: number
  temperature: number
}

interface MyFeed {
  feedId: number
  title: string
  category: string
  createdAt: string
}

// Mock data for demo
const mockCheeredAnimals: CheeredAnimal[] = [
  { animalId: 1, kind: "개", breed: "믹스견", imageUrl: "https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=200&h=200&fit=crop", heartCount: 5, temperature: 85.0 },
  { animalId: 2, kind: "고양이", breed: "코리안숏헤어", imageUrl: "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=200&h=200&fit=crop", heartCount: 3, temperature: 72.0 },
  { animalId: 3, kind: "개", breed: "말티즈", imageUrl: "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=200&h=200&fit=crop", heartCount: 2, temperature: 45.0 },
]

const mockMyFeeds: MyFeed[] = [
  { feedId: 101, title: "우리 강아지 입양 후기", category: "REVIEW", createdAt: "2025-01-15T10:30:00" },
  { feedId: 102, title: "유기동물 봉사활동 후기", category: "VOLUNTEER", createdAt: "2025-01-10T14:20:00" },
  { feedId: 103, title: "이 아이 입양 원해요", category: "PROMOTE", createdAt: "2025-01-05T09:15:00" },
]

const categoryLabels: Record<string, string> = {
  ADOPTION_REVIEW: "입양후기",
  VOLUNTEER: "봉사활동",
  FREE: "자유게시판",
  PROMOTE: "홍보",
  QUESTION: "질문",
}

const adoptionStatusLabels: Record<AdoptionStatus, string> = {
  Processing: "검토중",
  Approved: "승인",
  Rejected: "거절",
}

const adoptionStatusClass: Record<AdoptionStatus, string> = {
  Processing: "border-amber-200 bg-amber-50 text-amber-700",
  Approved: "border-emerald-200 bg-emerald-50 text-emerald-700",
  Rejected: "border-red-200 bg-red-50 text-red-700",
}

export default function ProfilePage() {
  const router = useRouter()
  const { user, isLoading: authLoading, updateUser } = useAuth()
  const [cheeredAnimals, setCheeredAnimals] = useState<CheeredAnimal[]>([])
  const [myFeeds, setMyFeeds] = useState<MyFeed[]>([])
  const [profileStats, setProfileStats] = useState<{
    feedCount: number,
    cheerCount: number,
    feedCommentCount: number,
    animalCommentCount: number,
    createdAt?: string
  }>({ feedCount: 0, cheerCount: 0, feedCommentCount: 0, animalCommentCount: 0 })
  const [isLoading, setIsLoading] = useState(true)
  const [showNicknameModal, setShowNicknameModal] = useState(false)
  const [showPasswordModal, setShowPasswordModal] = useState(false)
  const [showUsernameModal, setShowUsernameModal] = useState(false)
  const [showProfileImgModal, setShowProfileImgModal] = useState(false)
  const [myFeedComments, setMyFeedComments] = useState<MyFeedComment[]>([])
  const [myAnimalComments, setMyAnimalComments] = useState<MyAnimalComment[]>([])
  const [myAdoptions, setMyAdoptions] = useState<MyAdoptionApplication[]>([])
  const [adoptionError, setAdoptionError] = useState<string | null>(null)
  const [cancelingApplicationId, setCancelingApplicationId] = useState<number | null>(null)
  const [isWithdrawing, setIsWithdrawing] = useState(false)

  const handleCancelAdoption = async (applicationId: number) => {
    if (cancelingApplicationId) return

    const confirmed = window.confirm("입양 신청을 취소하시겠습니까?")
    if (!confirmed) return

    setCancelingApplicationId(applicationId)
    const { error, status } = await cancelMyAdoption(applicationId)
    setCancelingApplicationId(null)

    if (error || status !== 204) {
      alert(error || "입양 신청 취소에 실패했습니다.")
      return
    }

    setMyAdoptions((current) =>
      current.filter((application) => application.applicationId !== applicationId)
    )
  }

  const handleWithdraw = async () => {
    if (isWithdrawing) return

    const confirmed = window.confirm("정말 회원 탈퇴하시겠습니까?")
    if (!confirmed) return

    setIsWithdrawing(true)
    const { error, status } = await apiRequest<void>(API_ENDPOINTS.withdraw, {
      method: "DELETE",
    })
    setIsWithdrawing(false)

    if (error || status !== 204) {
      alert(error || "회원 탈퇴에 실패했습니다.")
      return
    }

    localStorage.removeItem("auth_token")
    localStorage.removeItem("user")
    alert("회원 탈퇴가 완료되었습니다.")
    window.location.href = "/"
  }

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      router.push("/login")
      return
    }

    const fetchProfileData = async () => {
      setIsLoading(true)

      // Fetch user profile info
      const profileResponse = await apiRequest<{
        profileImageUrl?: string
        nickname: string
        email: string
        name: string
        createdAt: string
      }>(API_ENDPOINTS.myProfile)

      if (profileResponse.data) {
        if (
          !user.createdAt ||
          user.nickname !== profileResponse.data.nickname ||
          user.email !== profileResponse.data.email ||
          user.name !== profileResponse.data.name
        ) {
          updateUser({
            nickname: profileResponse.data.nickname,
            email: profileResponse.data.email,
            name: profileResponse.data.name,
            createdAt: profileResponse.data.createdAt
          })
        }
      }

      // Fetch profile stats overview
      const statsResponse = await apiRequest<{
        feedCount: number,
        cheerCount: number,
        feedCommentCount: number,
        animalCommentCount: number,
        createdAt?: string
      }>(API_ENDPOINTS.myProfileStats)
      if (statsResponse.data) {
        setProfileStats(statsResponse.data)
      }

      // Fetch cheered animals
      const heartsResponse = await apiRequest<{
        totalAnimalCount: number, animals: CheeredAnimal[]
      }>(API_ENDPOINTS.myCheerAnimals)

      if (heartsResponse.data?.animals) {
        setCheeredAnimals(heartsResponse.data.animals)
      } else {
        // Use mock data for demo
        setCheeredAnimals(mockCheeredAnimals)
      }

      // Fetch my feeds
      const feedsResponse = await apiRequest<{
        totalFeedCount: number, feeds: MyFeed[]
      }>(API_ENDPOINTS.myFeeds)

      if (feedsResponse.data?.feeds) {
        setMyFeeds(feedsResponse.data.feeds)
      } else {
        // Use mock data for demo
        setMyFeeds(mockMyFeeds)
      }

      // Fetch my feed comments
      const feedCommentsResponse = await apiRequest<{
        totalCommentCount: number, comments: MyFeedComment[]
      }>(API_ENDPOINTS.myFeedComments)

      if (feedCommentsResponse.data?.comments) {
        setMyFeedComments(feedCommentsResponse.data.comments)
      }

      // Fetch my animal comments
      const animalCommentsResponse = await apiRequest<{
        totalCommentCount: number, comments: MyAnimalComment[]
      }>(API_ENDPOINTS.myAnimalComments)

      if (animalCommentsResponse.data?.comments) {
        setMyAnimalComments(animalCommentsResponse.data.comments)
      }

      const adoptionsResponse = await getMyAdoptions()
      if (adoptionsResponse.error) {
        setAdoptionError(adoptionsResponse.error)
        setMyAdoptions([])
      } else {
        setAdoptionError(null)
        setMyAdoptions(adoptionsResponse.data ?? [])
      }

      setIsLoading(false)
    }

    fetchProfileData()
  }, [user, router, authLoading])

  if (!user || authLoading) {
    return null
  }

  return (
    <div className="min-h-screen bg-background">
      <Header />

      <main className="max-w-4xl mx-auto px-4 py-8">
        {/* Profile Header */}
        <Card className="mb-8">
          <CardContent className="pt-6">
            <div className="flex flex-col sm:flex-row items-center gap-6">
              {/* Avatar */}
              <div
                className="relative w-24 h-24 rounded-full bg-primary/10 flex items-center justify-center overflow-hidden cursor-pointer group"
                onClick={() => setShowProfileImgModal(true)}
              >
                {user.profileImageUrl ? (
                  <Image
                    src={user.profileImageUrl}
                    alt={user.nickname || user.name}
                    fill
                    className="object-cover transition-transform group-hover:scale-105"
                  />
                ) : (
                  <UserIcon className="w-12 h-12 text-primary" />
                )}
                <div className="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                  <Settings className="w-6 h-6 text-white" />
                </div>
              </div>

              {/* User Info & Stats */}
              <div className="flex-1 w-full">
                <div className="hidden sm:block">
                  <h1 className="text-2xl font-bold text-foreground">{user.nickname || user.name}</h1>
                </div>

                {/* Stats Grid */}
                <div className="grid grid-cols-2 min-[400px]:grid-cols-3 sm:flex sm:flex-wrap items-center justify-start gap-x-4 md:gap-x-8 gap-y-4 mt-4 sm:mt-6">
                  <div className="text-left">
                    <p className="text-xl md:text-2xl font-bold text-primary">{profileStats.cheerCount}</p>
                    <p className="text-[10px] md:text-xs text-muted-foreground mt-1">보낸 응원</p>
                  </div>
                  <div className="text-left">
                    <p className="text-xl md:text-2xl font-bold text-primary">{cheeredAnimals.length}</p>
                    <p className="text-[10px] md:text-xs text-muted-foreground mt-1">응원한 동물</p>
                  </div>
                  <div className="text-left">
                    <p className="text-xl md:text-2xl font-bold text-primary">{profileStats.feedCount}</p>
                    <p className="text-[10px] md:text-xs text-muted-foreground mt-1">작성한 글</p>
                  </div>

                  {/* Divider for comments */}
                  <div className="hidden min-[400px]:block w-px h-8 bg-border self-center" />

                  <div className="text-left">
                    <p className="text-xl md:text-2xl font-bold text-foreground">{profileStats.feedCommentCount || 0}</p>
                    <p className="text-[10px] md:text-xs text-muted-foreground mt-1">피드 댓글</p>
                  </div>
                  <div className="text-left">
                    <p className="text-xl md:text-2xl font-bold text-foreground">{profileStats.animalCommentCount || 0}</p>
                    <p className="text-[10px] md:text-xs text-muted-foreground mt-1">동물 댓글</p>
                  </div>
                </div>
              </div>

              {/* Settings Button (Desktop) */}
              <div className="hidden sm:block">
                {/* <Button variant="outline" className="rounded-xl gap-2" onClick={() => setShowNicknameModal(true)}>
                  <Settings className="w-4 h-4" />
                  설정
                </Button> */}
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Tabs */}
        <Tabs defaultValue="account" className="space-y-6">
          <div className="overflow-x-auto pb-1 scrollbar-hide -mx-4 px-4 sm:mx-0 sm:px-0">
            <TabsList className="flex w-fit sm:w-full min-w-full rounded-xl bg-muted/50 p-1">
              <TabsTrigger value="account" className="flex-1 gap-2 rounded-xl py-2 px-3 whitespace-nowrap">
                <UserIcon className="w-4 h-4 shrink-0" />
                <span className="hidden sm:inline text-xs sm:text-sm">계정 정보</span>
              </TabsTrigger>
              <TabsTrigger value="hearts" className="flex-1 gap-2 rounded-xl py-2 px-3 whitespace-nowrap">
                <Heart className="w-4 h-4 shrink-0" />
                <span className="hidden sm:inline text-xs sm:text-sm">응원 내역</span>
              </TabsTrigger>
              <TabsTrigger value="posts" className="flex-1 gap-2 rounded-xl py-2 px-3 whitespace-nowrap">
                <FileText className="w-4 h-4 shrink-0" />
                <span className="hidden sm:inline text-xs sm:text-sm">작성한 글</span>
              </TabsTrigger>
              <TabsTrigger value="adoptions" className="flex-1 gap-2 rounded-xl py-2 px-3 whitespace-nowrap">
                <FileText className="w-4 h-4 shrink-0" />
                <span className="hidden sm:inline text-xs sm:text-sm">입양 신청</span>
                <span className="sm:hidden text-xs">입양</span>
              </TabsTrigger>
              <TabsTrigger value="feed-comments" className="flex-1 gap-2 rounded-xl py-2 px-3 whitespace-nowrap">
                <MessageSquare className="w-4 h-4 shrink-0" />
                <span className="text-xs sm:text-sm">
                  <span className="sm:hidden">피드</span>
                  <span className="hidden sm:inline">피드 댓글</span>
                </span>
              </TabsTrigger>
              <TabsTrigger value="animal-comments" className="flex-1 gap-2 rounded-xl py-2 px-3 whitespace-nowrap">
                <MessageSquare className="w-4 h-4 shrink-0" />
                <span className="text-xs sm:text-sm">
                  <span className="sm:hidden">동물</span>
                  <span className="hidden sm:inline">동물 댓글</span>
                </span>
              </TabsTrigger>
            </TabsList>
          </div>

          {/* Account Info Tab */}
          <TabsContent value="account">
            <Card>
              <CardHeader>
                <CardTitle>계정 정보</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div className="space-y-1">
                    <p className="text-sm text-muted-foreground">이메일</p>
                    <p className="font-medium">{user.email || "-"}</p>
                  </div>
                  <div className="space-y-1">
                    <p className="text-sm text-muted-foreground">이름</p>
                    <p className="font-medium">{user.name}</p>
                  </div>
                  <div className="space-y-1">
                    <p className="text-sm text-muted-foreground">닉네임</p>
                    <p className="font-medium">{user.nickname}</p>
                  </div>
                  <div className="space-y-1">
                    <p className="text-sm text-muted-foreground">가입일</p>
                    <p className="font-medium">
                      {(profileStats.createdAt || user.createdAt)
                        ? new Date(profileStats.createdAt || user.createdAt!).toLocaleDateString('ko-KR', {
                          year: 'numeric',
                          month: 'long',
                          day: 'numeric',
                        })
                        : "2025년 1월 1일"}
                    </p>
                  </div>
                </div>

                <div className="pt-4 border-t flex flex-wrap items-center gap-2">
                  {/* 
                  <Button variant="outline" className="rounded-xl" onClick={() => setShowUsernameModal(true)}>
                    아이디 변경
                  </Button>
                  */}
                  <Button variant="outline" className="rounded-xl" onClick={() => setShowPasswordModal(true)}>
                    비밀번호 변경
                  </Button>
                  <Button variant="outline" className="rounded-xl" onClick={() => setShowNicknameModal(true)}>
                    이름(닉네임) 변경
                  </Button>
                </div>
                <div className="flex justify-end pt-3">
                  <Button
                    className="rounded-xl bg-destructive text-white hover:bg-destructive/90"
                    onClick={handleWithdraw}
                    disabled={isWithdrawing}
                  >
                    {isWithdrawing ? "탈퇴 처리 중..." : "회원 탈퇴"}
                  </Button>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          {/* Hearts Tab */}
          <TabsContent value="hearts">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Heart className="w-5 h-5 text-primary" />
                  응원한 동물 ({cheeredAnimals.length})
                </CardTitle>
              </CardHeader>
              <CardContent>
                {isLoading ? (
                  <div className="text-center py-8 text-muted-foreground">
                    로딩 중...
                  </div>
                ) : cheeredAnimals.length === 0 ? (
                  <div className="text-center py-8">
                    <Heart className="w-12 h-12 text-muted-foreground/30 mx-auto mb-4" />
                    <p className="text-muted-foreground">아직 응원한 동물이 없습니다</p>
                    <Link href="/">
                      <Button className="mt-4 rounded-xl">동물 응원하러 가기</Button>
                    </Link>
                  </div>
                ) : (
                  <div className="space-y-3">
                    {cheeredAnimals.map((animal) => (
                      <Link
                        key={animal.animalId}
                        href={`/animals/${animal.animalId}`}
                        className="block"
                      >
                        <div className="flex items-center gap-4 p-3 rounded-xl hover:bg-secondary/50 transition-colors">
                          {/* Animal Image */}
                          <div className="relative w-16 h-16 rounded-xl overflow-hidden bg-secondary flex-shrink-0">
                            {animal.imageUrl ? (
                              <Image
                                src={animal.imageUrl}
                                alt={`${animal.kind} ${animal.breed || ""}`}
                                fill
                                className="object-cover"
                              />
                            ) : (
                              <div className="w-full h-full flex items-center justify-center text-muted-foreground">
                                {animal.kind === "개" ? "🐕" : "🐈"}
                              </div>
                            )}
                          </div>

                          {/* Animal Info */}
                          <div className="flex-1 min-w-0">
                            <p className="font-medium text-foreground">
                              {animal.kind} {animal.breed && `(${animal.breed})`}
                            </p>
                            <div className="flex items-center gap-4 mt-1 text-sm text-muted-foreground">
                              <span className="flex items-center gap-1">
                                <Heart className="w-3.5 h-3.5 fill-primary text-primary" />
                                {animal.heartCount}개 보냄
                              </span>
                              <span className="flex items-center gap-1">
                                <ThermometerSun className="w-3.5 h-3.5" />
                                {animal.temperature.toFixed(1)}C
                              </span>
                            </div>
                          </div>

                          {/* Arrow */}
                          <div className="text-muted-foreground">
                            &rarr;
                          </div>
                        </div>
                      </Link>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          {/* Posts Tab */}
          <TabsContent value="posts">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <FileText className="w-5 h-5 text-primary" />
                  작성한 글 ({myFeeds.length})
                </CardTitle>
              </CardHeader>
              <CardContent>
                {isLoading ? (
                  <div className="text-center py-8 text-muted-foreground">
                    로딩 중...
                  </div>
                ) : myFeeds.length === 0 ? (
                  <div className="text-center py-8">
                    <FileText className="w-12 h-12 text-muted-foreground/30 mx-auto mb-4" />
                    <p className="text-muted-foreground">아직 작성한 글이 없습니다</p>
                    <Link href="/community">
                      <Button className="mt-4 rounded-xl">커뮤니티 가기</Button>
                    </Link>
                  </div>
                ) : (
                  <div className="space-y-3">
                    {myFeeds.map((feed) => (
                      <Link
                        key={feed.feedId}
                        href={`/community/${feed.feedId}`}
                        className="block group"
                      >
                        <div className="flex items-center justify-between p-4 rounded-2xl hover:bg-secondary/50 transition-all border border-transparent hover:border-border/50">
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center gap-2 mb-2">
                              <span className="px-2 py-0.5 text-[10px] font-bold rounded-md bg-primary/10 text-primary uppercase">
                                {categoryLabels[feed.category] || feed.category}
                              </span>
                            </div>
                            <p className="font-semibold text-foreground truncate mb-1">
                              {feed.title}
                            </p>
                            <p className="text-xs text-muted-foreground flex items-center gap-1">
                              <Calendar className="w-3 h-3" />
                              {formatDate(feed.createdAt)}
                            </p>
                          </div>
                          <div className="ml-4 text-muted-foreground group-hover:text-primary transition-colors">
                            &rarr;
                          </div>
                        </div>
                      </Link>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="adoptions">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <FileText className="w-5 h-5 text-primary" />
                  입양 신청 내역 ({myAdoptions.length})
                </CardTitle>
              </CardHeader>
              <CardContent>
                {isLoading ? (
                  <div className="text-center py-8 text-muted-foreground">로딩 중...</div>
                ) : adoptionError ? (
                  <div className="rounded-xl border border-destructive/30 bg-destructive/5 p-4 text-sm text-destructive">
                    {adoptionError}
                  </div>
                ) : myAdoptions.length === 0 ? (
                  <div className="text-center py-8">
                    <FileText className="w-12 h-12 text-muted-foreground/30 mx-auto mb-4" />
                    <p className="text-muted-foreground">아직 입양 신청 내역이 없습니다</p>
                    <Link href="/">
                      <Button className="mt-4 rounded-xl">입양 가능한 동물 보기</Button>
                    </Link>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {myAdoptions.map((application) => (
                      <div
                        key={application.applicationId}
                        className="rounded-2xl border border-border/70 p-4"
                      >
                        <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                          <div className="min-w-0">
                            <div className="flex flex-wrap items-center gap-2">
                              <h3 className="font-semibold text-foreground">
                                {application.animalInfo.kindFullNm || application.animalInfo.desertionNo}
                              </h3>
                              <span className={`rounded-full border px-2 py-0.5 text-xs font-medium ${adoptionStatusClass[application.status]}`}>
                                {adoptionStatusLabels[application.status]}
                              </span>
                            </div>
                            <p className="mt-1 text-sm text-muted-foreground">
                              신청번호 {application.applicationId}
                              {application.createdAt ? ` · ${formatDate(application.createdAt)}` : ""}
                            </p>
                          </div>
                          {application.status === "Processing" && (
                            <Button
                              variant="outline"
                              size="sm"
                              className="w-fit rounded-xl"
                              onClick={() => handleCancelAdoption(application.applicationId)}
                              disabled={cancelingApplicationId === application.applicationId}
                            >
                              {cancelingApplicationId === application.applicationId ? "취소 중..." : "신청 취소"}
                            </Button>
                          )}
                        </div>

                        <div className="mt-4 grid gap-3 text-sm sm:grid-cols-2">
                          <div className="rounded-xl bg-secondary/40 p-3">
                            <p className="mb-1 text-xs font-medium text-muted-foreground">보호소</p>
                            <p className="font-medium text-foreground">
                              {application.animalInfo.careNm || "보호소 정보 없음"}
                            </p>
                            {application.animalInfo.careAddr && (
                              <p className="mt-2 flex items-start gap-1.5 text-xs text-muted-foreground">
                                <MapPin className="mt-0.5 h-3.5 w-3.5 shrink-0" />
                                <span>{application.animalInfo.careAddr}</span>
                              </p>
                            )}
                            {application.animalInfo.careTel && (
                              <p className="mt-1 flex items-center gap-1.5 text-xs text-muted-foreground">
                                <Phone className="h-3.5 w-3.5 shrink-0" />
                                <span>{application.animalInfo.careTel}</span>
                              </p>
                            )}
                          </div>

                          <div className="rounded-xl bg-secondary/40 p-3">
                            <p className="mb-1 text-xs font-medium text-muted-foreground">신청 연락처</p>
                            <p className="font-medium text-foreground">
                              {application.applyTel || "정보 없음"}
                            </p>
                            {application.reviewedAt && (
                              <p className="mt-2 text-xs text-muted-foreground">
                                심사일 {formatDate(application.reviewedAt)}
                              </p>
                            )}
                          </div>
                        </div>

                        {application.applyReason && (
                          <div className="mt-3 rounded-xl bg-muted/50 p-3 text-sm leading-6 text-foreground">
                            <p className="mb-1 text-xs font-medium text-muted-foreground">신청 사유</p>
                            {application.applyReason}
                          </div>
                        )}

                        {application.rejectionReason && (
                          <div className="mt-3 rounded-xl border border-red-200 bg-red-50 p-3 text-sm leading-6 text-red-700">
                            <p className="mb-1 text-xs font-medium">거절 사유</p>
                            {application.rejectionReason}
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          {/* Feed Comments Tab */}
          <TabsContent value="feed-comments">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <MessageSquare className="w-5 h-5 text-primary" />
                  작성한 피드 댓글 ({myFeedComments.length})
                </CardTitle>
              </CardHeader>
              <CardContent>
                {isLoading ? (
                  <div className="text-center py-8 text-muted-foreground">로딩 중...</div>
                ) : myFeedComments.length === 0 ? (
                  <div className="text-center py-8">
                    <MessageSquare className="w-12 h-12 text-muted-foreground/30 mx-auto mb-4" />
                    <p className="text-muted-foreground">아직 작성한 댓글이 없습니다</p>
                    <Link href="/community">
                      <Button className="mt-4 rounded-xl">커뮤니티 가기</Button>
                    </Link>
                  </div>
                ) : (
                  <div className="space-y-3">
                    {myFeedComments.map((comment, index) => (
                      <Link key={index} href={`/community/${comment.feedId}`} className="block group">
                        <div className="flex items-center justify-between p-4 rounded-2xl hover:bg-secondary/50 transition-all border border-border/50">
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center gap-2 mb-2">
                              <span className="px-2 py-0.5 text-[10px] font-bold rounded-md bg-primary/10 text-primary uppercase">
                                {categoryLabels[comment.category] || comment.category}
                              </span>
                            </div>
                            <p className="text-sm text-foreground mb-2 line-clamp-2 leading-relaxed">
                              "{comment.content}"
                            </p>
                            <p className="text-xs text-muted-foreground flex items-center gap-1">
                              <Calendar className="w-3 h-3" />
                              {formatDate(comment.createdAt)}
                            </p>
                          </div>
                          <div className="ml-4 text-muted-foreground group-hover:text-primary transition-colors text-xs whitespace-nowrap">
                            피드 보기 &rarr;
                          </div>
                        </div>
                      </Link>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          {/* Animal Comments Tab */}
          <TabsContent value="animal-comments">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <MessageSquare className="w-5 h-5 text-primary" />
                  작성한 동물 댓글 ({myAnimalComments.length})
                </CardTitle>
              </CardHeader>
              <CardContent>
                {isLoading ? (
                  <div className="text-center py-8 text-muted-foreground">로딩 중...</div>
                ) : myAnimalComments.length === 0 ? (
                  <div className="text-center py-8">
                    <MessageSquare className="w-12 h-12 text-muted-foreground/30 mx-auto mb-4" />
                    <p className="text-muted-foreground">아직 작성한 댓글이 없습니다</p>
                    <Link href="/">
                      <Button className="mt-4 rounded-xl">동물 보러 가기</Button>
                    </Link>
                  </div>
                ) : (
                  <div className="space-y-3">
                    {myAnimalComments.map((comment, index) => (
                      <Link key={index} href={`/animals/${comment.animalId || comment.feedId}`} className="block group">
                        <div className="flex items-center justify-between p-4 rounded-2xl hover:bg-secondary/50 transition-all border border-border/50">
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center gap-2 mb-2">
                              <span className="px-2 py-0.5 text-[10px] font-bold rounded-md bg-accent/20 text-accent-foreground uppercase">
                                동물 상세
                              </span>
                              {comment.desertionNo && (
                                <span className="text-[10px] text-muted-foreground px-1.5 py-0.5 bg-secondary rounded-md">
                                  No.{comment.desertionNo}
                                </span>
                              )}
                            </div>
                            <p className="text-sm text-foreground mb-2 line-clamp-2 leading-relaxed">
                              "{comment.content}"
                            </p>
                            <p className="text-xs text-muted-foreground flex items-center gap-1">
                              <Calendar className="w-3 h-3" />
                              {formatDate(comment.createdAt)}
                            </p>
                          </div>
                          <div className="ml-4 text-muted-foreground group-hover:text-primary transition-colors text-xs whitespace-nowrap">
                            상세 보기 &rarr;
                          </div>
                        </div>
                      </Link>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </main>

      {showNicknameModal && (
        <UpdateNicknameModal
          currentNickname={user.nickname || user.name}
          onClose={() => setShowNicknameModal(false)}
          onSuccess={(newName) => {
            setShowNicknameModal(false)
            updateUser({ nickname: newName }) // 로컬 닉네임 상태 변경
          }}
        />
      )}

      {showUsernameModal && (
        <UpdateUsernameModal
          currentUsername={user.username}
          onClose={() => setShowUsernameModal(false)}
          onSuccess={(newUsername) => {
            setShowUsernameModal(false)
            updateUser({ username: newUsername }) // 로컬 상태 변경
          }}
        />
      )}

      {showPasswordModal && (
        <UpdatePasswordModal
          onClose={() => setShowPasswordModal(false)}
        />
      )}

      {showProfileImgModal && (
        <UpdateProfileImgModal
          currentImageUrl={user.profileImageUrl || ""}
          onClose={() => setShowProfileImgModal(false)}
          onSuccess={(newUrl) => {
            setShowProfileImgModal(false)
            updateUser({ profileImageUrl: newUrl })
          }}
        />
      )}
    </div>
  )
}

function UpdateUsernameModal({ currentUsername, onClose, onSuccess }: { currentUsername: string, onClose: () => void, onSuccess: (newUsername: string) => void }) {
  const [username, setUsername] = useState(currentUsername)
  const [errorMessage, setErrorMessage] = useState("")

  const handleSubmit = async () => {
    setErrorMessage("")

    // 클라이언트 사이드 유효성 검증 (Regex)
    const usernameRegex = /^[a-zA-Z0-9!@#$%^&*()_+\-={}\[\]:;"'<>,.?/]{5,20}$/;

    if (!usernameRegex.test(username)) {
      setErrorMessage("id는 5~20자 사이의 영문, 숫자, 특수문자만 사용할 수 있습니다.");
      return;
    }

    const { data, error, errorCode } = await apiRequest(API_ENDPOINTS.updateUsername, {
      method: "PATCH",
      body: JSON.stringify({ newUsername: username }) // 'newUsername' 백엔드 필드 규격 준수
    })

    if (error || errorCode) {
      if (errorCode === "U-007" || error?.includes("사용 중인") || error?.includes("중복")) {
        setErrorMessage("이미 사용 중인 아이디입니다.")
      } else {
        setErrorMessage(error || "아이디 변경에 실패했습니다.")
      }
      return
    }

    alert("아이디가 성공적으로 변경되었습니다.")
    onSuccess(username)
  }

  return (
    <div className="fixed inset-0 bg-foreground/50 flex items-center justify-center z-50 p-4">
      <Card className="w-full max-w-sm border-0 shadow-2xl">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <CardTitle className="text-lg">아이디 변경</CardTitle>
          <Button variant="ghost" size="icon" onClick={onClose}>
            <X className="w-5 h-5" />
          </Button>
        </CardHeader>
        <CardContent className="space-y-4 pt-4">
          <div className="space-y-2">
            <label className="text-sm font-medium">새 아이디</label>
            <Input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="새로운 아이디를 입력하세요"
              className="rounded-xl"
            />
          </div>
          {errorMessage && (
            <p className="text-sm font-medium text-destructive">{errorMessage}</p>
          )}
          <div className="flex gap-2 w-full pt-4">
            <Button variant="outline" className="flex-1 rounded-xl" onClick={onClose}>취소</Button>
            <Button className="flex-1 rounded-xl bg-primary text-primary-foreground hover:bg-primary/90" onClick={handleSubmit}>변경하기</Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

function UpdateNicknameModal({ currentNickname, onClose, onSuccess }: { currentNickname: string, onClose: () => void, onSuccess: (newNickname: string) => void }) {
  const [nickname, setNickname] = useState(currentNickname)
  const [errorMessage, setErrorMessage] = useState("")

  const handleSubmit = async () => {
    setErrorMessage("")
    if (!nickname.trim()) {
      setErrorMessage("닉네임은 필수 입력값입니다.")
      return
    }

    if (nickname.length < 1 || nickname.length > 20) {
      setErrorMessage("닉네임은 1~20자 사이여야 합니다.")
      return
    }

    const { data, error, errorCode } = await apiRequest(API_ENDPOINTS.updateNickname, {
      method: "PATCH",
      body: JSON.stringify({ nickname })
    })

    if (error || errorCode) {
      if (errorCode === "U-007" || error?.includes("DUPLICATE_NICKNAME") || error?.includes("사용 중인")) {
        setErrorMessage("이미 사용 중인 닉네임입니다.")
      } else {
        setErrorMessage("닉네임 변경에 실패했습니다: " + (error || errorCode))
      }
      return
    }

    alert("닉네임이 성공적으로 변경되었습니다.")
    onSuccess(nickname)
  }

  return (
    <div className="fixed inset-0 bg-foreground/50 flex items-center justify-center z-50 p-4">
      <Card className="w-full max-w-sm border-0 shadow-2xl">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <CardTitle className="text-lg">닉네임 변경</CardTitle>
          <Button variant="ghost" size="icon" onClick={onClose}>
            <X className="w-5 h-5" />
          </Button>
        </CardHeader>
        <CardContent className="space-y-4 pt-4">
          <div className="space-y-2">
            <label className="text-sm font-medium">새 닉네임</label>
            <Input
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              placeholder="새로운 닉네임을 입력하세요"
              className="rounded-xl"
            />
          </div>
          {errorMessage && (
            <p className="text-sm font-medium text-destructive">{errorMessage}</p>
          )}
          <div className="flex gap-2 w-full pt-4">
            <Button variant="outline" className="flex-1 rounded-xl" onClick={onClose}>취소</Button>
            <Button className="flex-1 rounded-xl bg-primary text-primary-foreground hover:bg-primary/90" onClick={handleSubmit}>변경하기</Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

function UpdatePasswordModal({ onClose }: { onClose: () => void }) {
  const [currentPassword, setCurrentPassword] = useState("")
  const [newPassword, setNewPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [errorMessage, setErrorMessage] = useState("")

  const handleSubmit = async () => {
    setErrorMessage("")
    if (!currentPassword || !newPassword || !confirmPassword) {
      setErrorMessage("모든 비밀번호 필드를 입력해주세요.")
      return
    }

    if (newPassword !== confirmPassword) {
      setErrorMessage("새 비밀번호가 서로 일치하지 않습니다.")
      return
    }

    // 1. 클라이언트 사이드 유효성 검증 (Regex)
    // 로그에 찍힌 백엔드 패턴과 동일하게 설정합니다.
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-={}\[\]:;"'<>,.?/]).{8,16}$/;

    if (!passwordRegex.test(newPassword)) {
      setErrorMessage("비밀번호는 8~16자 사이이며 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다.");
      return;
    }

    // 2. 서버 요청
    const { data, error, errorCode } = await apiRequest(API_ENDPOINTS.updatePassword, {
      method: "PATCH",
      body: JSON.stringify({ currentPassword, newPassword })
    });

    // 3. 응답 처리
    if (error || errorCode) {
      // 백엔드 UserErrorCode 에넘 기준 매핑
      const errorMessages: Record<string, string> = {
        "U-004": "사용자 정보를 찾을 수 없습니다.",
        "U-005": "현재 비밀번호가 일치하지 않습니다.",
        "U-006": "기존 비밀번호와 다른 비밀번호를 입력해주세요.",
        "U-001": "로그인이 만료되었습니다. 다시 로그인해주세요."
      };

      // 1순위: 정의된 에러 코드 확인
      // 2순위: 백엔드 Validation 에러 메시지(error) 사용
      // 3순위: 기본 메시지
      const finalMessage = (errorCode && errorMessages[errorCode]) || error || "비밀번호 변경에 실패했습니다.";

      setErrorMessage(finalMessage);
      return;
    }

    // 성공 처리
    alert("비밀번호가 변경되었습니다.");
    onClose()
  }

  return (
    <div className="fixed inset-0 bg-foreground/50 flex items-center justify-center z-50 p-4">
      <Card className="w-full max-w-sm border-0 shadow-2xl">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <CardTitle className="text-lg">비밀번호 변경</CardTitle>
          <Button variant="ghost" size="icon" onClick={onClose}>
            <X className="w-5 h-5" />
          </Button>
        </CardHeader>
        <CardContent className="space-y-4 pt-4">
          <div className="space-y-2">
            <label className="text-sm font-medium">현재 비밀번호</label>
            <Input
              type="password"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              placeholder="현재 비밀번호를 입력하세요"
              className="rounded-xl"
            />
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium">새 비밀번호</label>
            <Input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="새로운 비밀번호"
              className="rounded-xl"
            />
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium">새 비밀번호 확인</label>
            <Input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="비밀번호 재입력"
              className="rounded-xl"
            />
          </div>
          {errorMessage && (
            <p className="text-sm font-medium text-destructive">{errorMessage}</p>
          )}
          <div className="flex gap-2 w-full pt-4">
            <Button variant="outline" className="flex-1 rounded-xl" onClick={onClose}>취소</Button>
            <Button className="flex-1 rounded-xl bg-primary text-primary-foreground hover:bg-primary/90" onClick={handleSubmit}>변경하기</Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
function UpdateProfileImgModal({ currentImageUrl, onClose, onSuccess }: { currentImageUrl: string, onClose: () => void, onSuccess: (newUrl: string) => void }) {
  const [imageUrl, setImageUrl] = useState(currentImageUrl)
  const [errorMessage, setErrorMessage] = useState("")
  const [isLoading, setIsLoading] = useState(false)

  const handleSubmit = async () => {
    setErrorMessage("")
    if (!imageUrl.trim()) {
      setErrorMessage("이미지 URL을 입력해주세요.")
      return
    }

    setIsLoading(true)
    const { data, error, errorCode } = await apiRequest<User>(API_ENDPOINTS.updateProfileImg, {
      method: "PATCH",
      body: JSON.stringify({ profileImageUrl: imageUrl })
    })
    setIsLoading(false)

    if (error || errorCode) {
      setErrorMessage(error || "프로필 이미지 변경에 실패했습니다.")
      return
    }

    alert("프로필 이미지가 성공적으로 변경되었습니다.")
    onSuccess(imageUrl)
  }

  return (
    <div className="fixed inset-0 bg-foreground/50 flex items-center justify-center z-50 p-4">
      <Card className="w-full max-w-sm border-0 shadow-2xl">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <CardTitle className="text-lg">프로필 이미지 변경</CardTitle>
          <Button variant="ghost" size="icon" onClick={onClose} disabled={isLoading}>
            <X className="w-5 h-5" />
          </Button>
        </CardHeader>
        <CardContent className="space-y-4 pt-4">
          <div className="space-y-2">
            <label className="text-sm font-medium">이미지 URL</label>
            <div className="flex gap-4 items-center mb-4">
              <div className="w-16 h-16 rounded-full bg-muted overflow-hidden flex-shrink-0">
                {imageUrl ? (
                  <img src={imageUrl} alt="Preview" className="w-full h-full object-cover" />
                ) : (
                  <div className="w-full h-full flex items-center justify-center text-muted-foreground">
                    <UserIcon className="w-8 h-8" />
                  </div>
                )}
              </div>
              <p className="text-xs text-muted-foreground">이미지 URL을 입력하면 실시간으로 미리보기가 표시됩니다.</p>
            </div>
            <Input
              value={imageUrl}
              onChange={(e) => setImageUrl(e.target.value)}
              placeholder="https://example.com/image.jpg"
              className="rounded-xl"
              disabled={isLoading}
            />
          </div>
          {errorMessage && (
            <p className="text-sm font-medium text-destructive">{errorMessage}</p>
          )}
          <div className="flex gap-2 w-full pt-4">
            <Button variant="outline" className="flex-1 rounded-xl" onClick={onClose} disabled={isLoading}>취소</Button>
            <Button
              className="flex-1 rounded-xl bg-primary text-primary-foreground hover:bg-primary/90"
              onClick={handleSubmit}
              disabled={isLoading}
            >
              {isLoading ? "변경 중..." : "변경하기"}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
