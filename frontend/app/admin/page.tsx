"use client"

import { useEffect, useMemo, useState } from "react"
import Link from "next/link"
import { Building2, Check, RefreshCw } from "lucide-react"
import { Header } from "@/components/header"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { cn } from "@/lib/utils"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Textarea } from "@/components/ui/textarea"
import { useAuth } from "@/lib/auth-context"
import {
  AdminAdoptionApplication,
  AdminNameCandidate,
  AdoptionStatus,
  Campaign,
  confirmNamingCandidate,
  createCampaign,
  getAdminReadyNamingCandidates,
  getAdminShelterApplications,
  getShelterCampaign,
  getShelterDetail,
  isAdminUser,
  reviewAdminShelterApplication,
  updateCampaignStatus,
} from "@/lib/api"

const ADMIN_SHELTERS_BY_EMAIL: Record<string, { careRegNo: string; managerName: string }> = {
  "kim.minjun@petmeeting.local": { careRegNo: "345479202100001", managerName: "김민준" },
  "lee.seoyeon@petmeeting.local": { careRegNo: "345471201800002", managerName: "이서연" },
  "park.jihun@petmeeting.local": { careRegNo: "341390201300001", managerName: "박지훈" },
  "choi.hayoon@petmeeting.local": { careRegNo: "346489202000001", managerName: "최하윤" },
  "jung.doyun@petmeeting.local": { careRegNo: "345467201000001", managerName: "정도윤" },
  "kang.subin@petmeeting.local": { careRegNo: "346485202600001", managerName: "강수빈" },
  "yoon.jiho@petmeeting.local": { careRegNo: "348540200900001", managerName: "윤지호" },
}

const statusLabels: Record<AdoptionStatus, string> = {
  Processing: "검토중",
  Approved: "승인",
  Rejected: "거절",
}

const statusBadgeClass: Record<AdoptionStatus, string> = {
  Processing: "border-amber-200 bg-amber-50 text-amber-700",
  Approved: "border-emerald-200 bg-emerald-50 text-emerald-700",
  Rejected: "border-red-200 bg-red-50 text-red-700",
}

export default function AdminPage() {
  const { user, isLoading: authLoading } = useAuth()
  const isAdmin = isAdminUser(user)

  const [nameCandidates, setNameCandidates] = useState<AdminNameCandidate[]>([])
  const [nameError, setNameError] = useState<string | null>(null)
  const [isLoadingNames, setIsLoadingNames] = useState(false)
  const [confirmingCandidateId, setConfirmingCandidateId] = useState<number | null>(null)

  const [careRegNo, setCareRegNo] = useState("")
  const [shelterName, setShelterName] = useState<string | null>(null)
  const [applications, setApplications] = useState<AdminAdoptionApplication[]>([])
  const [applicationError, setApplicationError] = useState<string | null>(null)
  const [isLoadingApplications, setIsLoadingApplications] = useState(false)
  const [reviewingApplicationId, setReviewingApplicationId] = useState<number | null>(null)
  const [rejectionReasons, setRejectionReasons] = useState<Record<number, string>>({})

  // Campaign States
  const [shelterCampaigns, setShelterCampaigns] = useState<Campaign[]>([])
  const [isLoadingCampaigns, setIsLoadingCampaigns] = useState(false)
  const [campaignError, setCampaignError] = useState<string | null>(null)
  const [newCampaign, setNewCampaign] = useState({ title: "", description: "", amount: "" })
  const [isCreatingCampaign, setIsCreatingCampaign] = useState(false)

  const loadShelterCampaigns = async () => {
    if (!trimmedCareRegNo) return
    setIsLoadingCampaigns(true)
    setCampaignError(null)
    const { data, error } = await getShelterCampaign(trimmedCareRegNo)
    setIsLoadingCampaigns(false)
    if (error) {
      setCampaignError(error)
      return
    }
    setShelterCampaigns(data?.campaigns || [])
  }

  const handleCreateCampaign = async () => {
    if (!newCampaign.title.trim() || !newCampaign.amount) {
      alert("제목과 목표 금액을 입력해주세요.")
      return
    }

    const hasActive = shelterCampaigns.some(c => c.status === "ACTIVE")
    if (hasActive) {
      alert("이미 진행 중인 캠페인이 있습니다. 기존 캠페인을 종료한 후 새로 생성해주세요.")
      return
    }

    setIsCreatingCampaign(true)
    const { error } = await createCampaign(trimmedCareRegNo, {
      title: newCampaign.title,
      description: newCampaign.description,
      amount: Number(newCampaign.amount)
    })
    setIsCreatingCampaign(true) // wait reset below

    if (error) {
      alert(error)
      setIsCreatingCampaign(false)
      return
    }

    alert("캠페인이 성공적으로 생성되었습니다.")
    setNewCampaign({ title: "", description: "", amount: "" })
    await loadShelterCampaigns()
    setIsCreatingCampaign(false)
  }

  const handleCloseCampaign = async (campaignId: number) => {
    if (!confirm("캠페인을 종료하시겠습니까? 종료 후에는 더 이상 후원을 받을 수 없습니다.")) return

    const { error } = await updateCampaignStatus(campaignId)
    if (error) {
      alert(error)
      return
    }

    alert("캠페인이 종료되었습니다.")
    await loadShelterCampaigns()
  }

  const pendingApplications = useMemo(
    () => applications.filter((application) => application.status === "Processing").length,
    [applications]
  )

  const trimmedCareRegNo = careRegNo.trim()
  const adminIdentifier = (user?.username || user?.email || "").trim().toLowerCase()
  const assignedShelter = adminIdentifier ? ADMIN_SHELTERS_BY_EMAIL[adminIdentifier] : undefined

  const loadNameCandidates = async () => {
    setIsLoadingNames(true)
    setNameError(null)
    const { data, error } = await getAdminReadyNamingCandidates()
    setIsLoadingNames(false)

    if (error) {
      setNameError(error)
      setNameCandidates([])
      return
    }

    setNameCandidates(data ?? [])
  }

  const loadApplications = async () => {
    if (!trimmedCareRegNo) {
      setApplicationError("보호소 등록번호를 입력해주세요.")
      return
    }

    setIsLoadingApplications(true)
    setApplicationError(null)
    await resolveShelterName()
    const { data, error } = await getAdminShelterApplications(trimmedCareRegNo)
    setIsLoadingApplications(false)

    if (error) {
      setApplicationError(error)
      setApplications([])
      return
    }

    setApplications(data ?? [])
  }

  const resolveShelterName = async () => {
    if (shelterName) return shelterName
    const { data, error } = await getShelterDetail(trimmedCareRegNo)
    if (error || !data) {
      const message = error || "보호소 정보를 확인할 수 없습니다."
      setNameError(message)
      setApplicationError(message)
      return null
    }

    setShelterName(data.careNm)
    return data.careNm
  }

  const handleConfirmName = async (candidateId: number) => {
    setConfirmingCandidateId(candidateId)
    const { error } = await confirmNamingCandidate(candidateId)
    setConfirmingCandidateId(null)

    if (error) {
      alert(error)
      return
    }

    setNameCandidates((current) => current.filter((candidate) => candidate.candidateId !== candidateId))
  }

  const handleReviewApplication = async (applicationId: number, status: AdoptionStatus) => {
    const rejectionReason = rejectionReasons[applicationId]?.trim() || null
    if (status === "Rejected" && !rejectionReason) {
      alert("거절 사유를 입력해주세요.")
      return
    }

    setReviewingApplicationId(applicationId)
    const { data, error } = await reviewAdminShelterApplication(trimmedCareRegNo, applicationId, {
      status,
      rejectionReason: status === "Rejected" ? rejectionReason : null,
    })
    setReviewingApplicationId(null)

    if (error) {
      alert(error)
      return
    }

    if (data) {
      setApplications((current) =>
        current.map((application) =>
          application.applicationId === applicationId ? { ...application, ...data } : application
        )
      )
    }
  }

  useEffect(() => {
    if (!authLoading && isAdmin) {
      setShelterName(null)
      setApplications([])
      setNameCandidates([])
      setCareRegNo(assignedShelter?.careRegNo ?? "")
    }
  }, [authLoading, isAdmin, assignedShelter?.careRegNo])

  useEffect(() => {
    if (!authLoading && isAdmin && assignedShelter && trimmedCareRegNo) {
      loadApplications()
      loadShelterCampaigns()
    }
  }, [authLoading, isAdmin, assignedShelter, trimmedCareRegNo])

  if (authLoading) return null

  if (!user) {
    return (
      <div className="min-h-screen bg-background">
        <Header />
        <main className="max-w-3xl mx-auto px-4 py-10">
          <Alert>
            <Building2 className="h-4 w-4" />
            <AlertTitle>로그인이 필요합니다</AlertTitle>
            <AlertDescription>
              보호소 관리자 페이지는 로그인 후 접근할 수 있습니다.
              <Button asChild className="mt-4 w-fit">
                <Link href="/login">로그인</Link>
              </Button>
            </AlertDescription>
          </Alert>
        </main>
      </div>
    )
  }

  if (!isAdmin) {
    return (
      <div className="min-h-screen bg-background">
        <Header />
        <main className="max-w-3xl mx-auto px-4 py-10">
          <Alert variant="destructive">
            <Building2 className="h-4 w-4" />
            <AlertTitle>보호소 관리자 권한이 없습니다</AlertTitle>
            <AlertDescription>
              현재 계정의 권한은 {user.role || user.roles?.join(", ") || "일반 사용자"}입니다.
            </AlertDescription>
          </Alert>
        </main>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-background">
      <Header />

      <main className="max-w-6xl mx-auto px-4 py-8">
        <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <div className="mb-2 flex items-center gap-2 text-sm font-medium text-primary">
              <Building2 className="h-4 w-4" />
              보호소 관리자
            </div>
            <h1 className="text-3xl font-bold text-foreground">보호소 관리자 페이지</h1>
            <p className="mt-2 text-sm text-muted-foreground">
              담당 보호소의 입양 신청과 이름 확정을 처리합니다.
            </p>
          </div>
          <div className="grid grid-cols-2 gap-3 sm:w-72">
            <Card>
              <CardContent className="p-4">
                <p className="text-sm text-muted-foreground">보호소 이름 후보</p>
                <p className="mt-1 text-2xl font-bold">{nameCandidates.length}</p>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-4">
                <p className="text-sm text-muted-foreground">검토중 신청</p>
                <p className="mt-1 text-2xl font-bold">{pendingApplications}</p>
              </CardContent>
            </Card>
          </div>
        </div>

        <Card className="mb-4">
          <CardHeader>
            <CardTitle>담당 보호소</CardTitle>
            <CardDescription>
              {assignedShelter
                ? `${assignedShelter.managerName} 보호소 관리자 계정에 연결된 보호소입니다.`
                : "현재 계정에 연결된 보호소 정보를 찾을 수 없습니다."}
            </CardDescription>
          </CardHeader>
          <CardContent className="grid gap-3 sm:grid-cols-2">
            <div>
              <p className="text-sm text-muted-foreground">보호소 등록번호</p>
              <p className="mt-1 font-medium text-foreground">{trimmedCareRegNo || "미지정"}</p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">보호소명</p>
              <p className="mt-1 font-medium text-foreground">{shelterName || "확인 중"}</p>
            </div>
          </CardContent>
        </Card>

        <Tabs defaultValue="adoptions" className="space-y-4">
          <TabsList>
            <TabsTrigger value="adoptions">입양 신청</TabsTrigger>
            <TabsTrigger value="naming">이름 확정</TabsTrigger>
            <TabsTrigger value="campaigns">캠페인 관리</TabsTrigger>
          </TabsList>

          <TabsContent value="naming">
            <Card>
              <CardHeader className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <CardTitle>보호소 동물 이름 확정</CardTitle>
                  <CardDescription>담당 보호소 동물의 이름 후보를 확인하고 확정합니다.</CardDescription>
                </div>
                <Button variant="outline" onClick={loadNameCandidates} disabled={isLoadingNames}>
                  <RefreshCw className={`mr-2 h-4 w-4 ${isLoadingNames ? "animate-spin" : ""}`} />
                  새로고침
                </Button>
              </CardHeader>
              <CardContent>
                {nameError ? (
                  <Alert variant="destructive">
                    <AlertTitle>목록을 불러오지 못했습니다</AlertTitle>
                    <AlertDescription>{nameError}</AlertDescription>
                  </Alert>
                ) : isLoadingNames ? (
                  <div className="rounded-md border border-dashed py-10 text-center text-sm text-muted-foreground">
                    후보 목록을 불러오는 중...
                  </div>
                ) : nameCandidates.length === 0 ? (
                  <div className="rounded-md border border-dashed py-10 text-center text-sm text-muted-foreground">
                    담당 보호소에서 확정할 이름 후보가 없습니다.
                  </div>
                ) : (
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>동물</TableHead>
                        <TableHead>보호소</TableHead>
                        <TableHead>후보 이름</TableHead>
                        <TableHead>제안자</TableHead>
                        <TableHead>득표</TableHead>
                        <TableHead className="text-right">처리</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {nameCandidates.map((candidate) => (
                        <TableRow key={`${candidate.candidateId}-${candidate.animalId}`}>
                          <TableCell>
                            <div className="font-medium">{candidate.animalName || candidate.kindFullNm}</div>
                            <div className="text-xs text-muted-foreground">{candidate.desertionNo}</div>
                          </TableCell>
                          <TableCell>{candidate.careNm || trimmedCareRegNo}</TableCell>
                          <TableCell>{candidate.proposedName}</TableCell>
                          <TableCell>{candidate.proposerNickname}</TableCell>
                          <TableCell>{candidate.voteCount}</TableCell>
                          <TableCell className="text-right">
                            <Button
                              size="sm"
                              onClick={() => handleConfirmName(candidate.candidateId)}
                              disabled={confirmingCandidateId === candidate.candidateId || !candidate.candidateId}
                            >
                              <Check className="mr-2 h-4 w-4" />
                              확정
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="campaigns">
            <div className="grid gap-6 md:grid-cols-2">
              {/* Create Campaign Form */}
              <Card>
                <CardHeader>
                  <CardTitle>새 캠페인 생성</CardTitle>
                  <CardDescription>보호소의 새로운 후원 캠페인을 만듭니다. (동시 진행은 하나만 가능)</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="campaign-title">캠페인 제목</Label>
                    <Input 
                      id="campaign-title" 
                      placeholder="예: 겨울나기 난방비 모금" 
                      value={newCampaign.title}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) => setNewCampaign(prev => ({ ...prev, title: e.target.value }))}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="campaign-amount">목표 금액 (원)</Label>
                    <Input 
                      id="campaign-amount" 
                      type="number" 
                      placeholder="예: 1000000" 
                      value={newCampaign.amount}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) => setNewCampaign(prev => ({ ...prev, amount: e.target.value }))}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="campaign-desc">상세 설명</Label>
                    <Textarea 
                      id="campaign-desc" 
                      placeholder="캠페인에 대한 상세 설명을 입력하세요." 
                      className="min-h-[100px]"
                      value={newCampaign.description}
                      onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setNewCampaign(prev => ({ ...prev, description: e.target.value }))}
                    />
                  </div>
                  <Button 
                    className="w-full" 
                    onClick={handleCreateCampaign}
                    disabled={isCreatingCampaign || shelterCampaigns.some(c => c.status === "ACTIVE")}
                  >
                    {isCreatingCampaign ? "생성 중..." : "캠페인 시작하기"}
                  </Button>
                  {shelterCampaigns.some(c => c.status === "ACTIVE") && (
                    <p className="text-center text-xs text-destructive">
                      이미 진행 중인 캠페인이 있습니다.
                    </p>
                  )}
                </CardContent>
              </Card>

              {/* Campaign List */}
              <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                  <div>
                    <CardTitle>캠페인 현황</CardTitle>
                    <CardDescription>최근 진행되었거나 진행 중인 캠페인 목록입니다.</CardDescription>
                  </div>
                  <Button variant="outline" size="sm" onClick={loadShelterCampaigns} disabled={isLoadingCampaigns}>
                    <RefreshCw className={cn("h-4 w-4", isLoadingCampaigns && "animate-spin")} />
                  </Button>
                </CardHeader>
                <CardContent>
                  {campaignError ? (
                    <Alert variant="destructive">
                      <AlertDescription>{campaignError}</AlertDescription>
                    </Alert>
                  ) : shelterCampaigns.length === 0 ? (
                    <div className="py-8 text-center text-sm text-muted-foreground">
                      생성된 캠페인이 없습니다.
                    </div>
                  ) : (
                    <div className="space-y-4">
                      {shelterCampaigns.map((campaign) => (
                        <div key={campaign.id} className="rounded-lg border p-4">
                          <div className="flex items-start justify-between mb-2">
                            <div>
                              <div className="flex items-center gap-2">
                                <h4 className="font-bold">{campaign.title}</h4>
                                <Badge variant={campaign.status === "ACTIVE" ? "default" : "secondary"}>
                                  {campaign.status === "ACTIVE" ? "진행중" : campaign.status === "COMPLETE" ? "달성완료" : "종료"}
                                </Badge>
                              </div>
                              <p className="text-sm text-muted-foreground mt-1">
                                {campaign.currentAmount.toLocaleString()}원 / {campaign.targetAmount.toLocaleString()}원
                              </p>
                            </div>
                            {campaign.status === "ACTIVE" && (
                              <Button 
                                variant="outline" 
                                size="sm" 
                                className="text-destructive hover:text-destructive"
                                onClick={() => handleCloseCampaign(campaign.id)}
                              >
                                종료하기
                              </Button>
                            )}
                          </div>
                          {campaign.status === "ACTIVE" && (
                            <div className="w-full bg-secondary h-1.5 rounded-full overflow-hidden mt-3">
                              <div 
                                className="bg-primary h-full transition-all" 
                                style={{ width: `${Math.min(100, Math.round((campaign.currentAmount / campaign.targetAmount) * 100))}%` }}
                              />
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>
          </TabsContent>
          <TabsContent value="adoptions">
            <Card>
              <CardHeader>
                <CardTitle>입양 신청 관리</CardTitle>
                <CardDescription>담당 보호소 등록번호로 접수된 신청을 조회하고 처리합니다.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex flex-col gap-2 sm:flex-row sm:items-end">
                  <Button onClick={loadApplications} disabled={isLoadingApplications}>
                    <RefreshCw className="mr-2 h-4 w-4" />
                    새로고침
                  </Button>
                </div>

                {applicationError ? (
                  <Alert variant="destructive">
                    <AlertTitle>신청 목록을 불러오지 못했습니다</AlertTitle>
                    <AlertDescription>{applicationError}</AlertDescription>
                  </Alert>
                ) : applications.length === 0 ? (
                  <div className="rounded-md border border-dashed py-10 text-center text-sm text-muted-foreground">
                    조회된 입양 신청이 없습니다.
                  </div>
                ) : (
                  <div className="space-y-3">
                    {applications.map((application) => (
                      <Card key={application.applicationId}>
                        <CardContent className="space-y-4 p-4">
                          <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                            <div>
                              <div className="flex flex-wrap items-center gap-2">
                                <h3 className="font-semibold">
                                  {application.animalInfo.kindFullNm || application.animalInfo.desertionNo}
                                </h3>
                                <Badge variant="outline" className={statusBadgeClass[application.status]}>
                                  {statusLabels[application.status]}
                                </Badge>
                              </div>
                              <p className="mt-1 text-sm text-muted-foreground">
                                신청번호 {application.applicationId} · {application.animalInfo.careNm || "보호소 정보 없음"}
                              </p>
                            </div>
                            <div className="flex gap-2">
                              <Button
                                size="sm"
                                variant="outline"
                                onClick={() => handleReviewApplication(application.applicationId, "Approved")}
                                disabled={reviewingApplicationId === application.applicationId}
                              >
                                승인
                              </Button>
                              <Button
                                size="sm"
                                variant="destructive"
                                onClick={() => handleReviewApplication(application.applicationId, "Rejected")}
                                disabled={reviewingApplicationId === application.applicationId}
                              >
                                거절
                              </Button>
                            </div>
                          </div>

                          <div className="grid gap-3 text-sm sm:grid-cols-2">
                            <div>
                              <p className="text-muted-foreground">신청 연락처</p>
                              <p className="font-medium">{application.applyTel || "정보 없음"}</p>
                            </div>
                            <div>
                              <p className="text-muted-foreground">접수일</p>
                              <p className="font-medium">
                                {application.createdAt ? new Date(application.createdAt).toLocaleDateString("ko-KR") : "정보 없음"}
                              </p>
                            </div>
                          </div>

                          {application.applyReason && (
                            <div className="rounded-md bg-muted/50 p-3 text-sm leading-6">
                              {application.applyReason}
                            </div>
                          )}

                          <div className="space-y-2">
                            <Label htmlFor={`reason-${application.applicationId}`}>거절 사유</Label>
                            <Textarea
                              id={`reason-${application.applicationId}`}
                              value={rejectionReasons[application.applicationId] ?? ""}
                              onChange={(event) =>
                                setRejectionReasons((current) => ({
                                  ...current,
                                  [application.applicationId]: event.target.value,
                                }))
                              }
                              placeholder="거절 처리 시 사유를 입력하세요."
                            />
                          </div>
                        </CardContent>
                      </Card>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </main>
    </div>
  )
}
