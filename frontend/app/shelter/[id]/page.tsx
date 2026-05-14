"use client"

import { useEffect, useState, use } from "react"
import Link from "next/link"
import { Header } from "@/components/header"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Progress } from "@/components/ui/progress"
import { Button } from "@/components/ui/button"
import { Heart, ArrowLeft, Phone, MapPin, Info } from "lucide-react"
import { getShelterCampaign, getShelterDetail, type Campaign, type Shelter } from "@/lib/api"
import { cn } from "@/lib/utils"
import { DonationModal } from "@/components/donation-modal"

export default function ShelterPage({ params }: { params: Promise<{ id: string }> }) {
  const resolvedParams = use(params)
  const shelterId = resolvedParams.id
  const [shelter, setShelter] = useState<Shelter | null>(null)
  const [campaigns, setCampaigns] = useState<Campaign[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [isDonationModalOpen, setIsDonationModalOpen] = useState(false)
  const [selectedCampaign, setSelectedCampaign] = useState<Campaign | null>(null)

  useEffect(() => {
    const fetchData = async () => {
      setIsLoading(true)
      
      // Fetch both shelter info and campaign
      const [shelterRes, campaignRes] = await Promise.all([
        getShelterDetail(shelterId),
        getShelterCampaign(shelterId)
      ])

      if (shelterRes.error) {
        setError(shelterRes.error)
      } else {
        setShelter(shelterRes.data)
      }

      if (campaignRes.data) {
        setCampaigns(campaignRes.data.campaigns || [])
      }
      
      setIsLoading(false)
    }

    fetchData()
  }, [shelterId])

  if (isLoading) {
    return (
      <div className="min-h-screen bg-background">
        <Header />
        <div className="max-w-4xl mx-auto px-4 py-20 flex justify-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
        </div>
      </div>
    )
  }

  if (error || !shelter) {
    return (
      <div className="min-h-screen bg-background">
        <Header />
        <main className="max-w-4xl mx-auto px-4 py-20 text-center">
          <div className="w-16 h-16 bg-destructive/10 text-destructive rounded-2xl flex items-center justify-center mx-auto mb-4">
            <Info className="w-8 h-8" />
          </div>
          <h1 className="text-2xl font-bold mb-2">보호소 정보를 찾을 수 없습니다</h1>
          <p className="text-muted-foreground mb-8">{error || "해당 ID의 보호소가 존재하지 않습니다."}</p>
          <Link href="/campaign">
            <Button variant="outline">캠페인 목록으로 돌아가기</Button>
          </Link>
        </main>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-background">
      <Header />
      
      <main className="max-w-4xl mx-auto px-4 py-8">
        <Link href="/campaign" className="inline-flex items-center gap-2 text-muted-foreground hover:text-foreground transition-colors mb-8">
          <ArrowLeft className="w-4 h-4" />
          <span>캠페인 목록으로 돌아가기</span>
        </Link>

        <div className="space-y-8">
          {/* Shelter Header */}
          <section>
            <Card className="border-0 bg-card shadow-sm overflow-hidden">
              <CardContent className="p-0">
                <div className="bg-primary/5 p-8 border-b border-primary/10">
                  <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
                    <div className="flex items-center gap-6">
                      <div className="w-20 h-20 rounded-2xl bg-primary text-primary-foreground flex items-center justify-center shadow-lg">
                        <LandmarkIcon className="w-10 h-10" />
                      </div>
                      <div>
                        <h1 className="text-3xl font-bold mb-1">{shelter.careNm}</h1>
                        <p className="text-primary font-medium">{shelter.orgNm}</p>
                      </div>
                    </div>
                  </div>
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-8 p-8">
                  <div className="space-y-6">
                    <h3 className="text-lg font-semibold flex items-center gap-2 text-foreground">
                      <Info className="w-5 h-5 text-primary" />
                      연락처 및 위치
                    </h3>
                    <div className="space-y-4">
                      <div className="flex items-start gap-4 p-4 rounded-xl bg-secondary/30">
                        <Phone className="w-5 h-5 text-primary mt-0.5 shrink-0" />
                        <div>
                          <p className="text-xs text-muted-foreground mb-1">전화번호</p>
                          <a href={`tel:${shelter.careTel}`} className="font-bold text-foreground hover:text-primary transition-colors">
                            {shelter.careTel || "번호 정보 없음"}
                          </a>
                        </div>
                      </div>
                      <div className="flex items-start gap-4 p-4 rounded-xl bg-secondary/30">
                        <MapPin className="w-5 h-5 text-primary mt-0.5 shrink-0" />
                        <div>
                          <p className="text-xs text-muted-foreground mb-1">상세 주소</p>
                          <p className="font-medium text-foreground leading-relaxed">
                            {shelter.careAddr || "주소 정보 없음"}
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </section>

          {/* Active Campaign Section */}
          <section>
            <div className="flex items-center gap-3 mb-6">
              <Heart className="w-6 h-6 text-primary fill-primary" />
              <h2 className="text-2xl font-bold text-foreground">진행 중인 캠페인</h2>
            </div>

            {isLoading ? (
              <Card className="animate-pulse border-0 bg-card shadow-sm h-64" />
            ) : campaigns.length > 0 ? (
              <div className="space-y-6">
                {campaigns.map((campaign) => {
                  const progress = Math.min(100, Math.round((campaign.currentAmount / campaign.targetAmount) * 100))
                  return (
                    <Card key={campaign.id} className="border-0 bg-card shadow-sm hover:shadow-md transition-shadow duration-300">
                      <CardHeader className="pb-2">
                        <div className="flex justify-between items-start">
                          <CardTitle className="text-2xl font-bold">{campaign.title}</CardTitle>
                          <span className={cn(
                            "text-xs font-bold px-3 py-1 rounded-full",
                            campaign.status === "ACTIVE" ? "bg-success/10 text-success" : 
                            campaign.status === "COMPLETE" ? "bg-primary/10 text-primary" :
                            "bg-muted text-muted-foreground"
                          )}>
                            {campaign.status === "ACTIVE" ? "진행중" : 
                             campaign.status === "COMPLETE" ? "달성완료" : "종료"}
                          </span>
                        </div>
                      </CardHeader>
                      <CardContent className="pt-6 space-y-6">
                        <p className="text-sm text-muted-foreground leading-relaxed">
                          {campaign.description || "이 캠페인은 보호소의 아이들이 더 좋은 환경에서 지낼 수 있도록 도움을 줍니다."}
                        </p>
                        <div className="space-y-4">
                          <div className="flex justify-between items-end">
                            <div className="space-y-1">
                              <p className="text-sm text-muted-foreground">목표 달성률</p>
                              <div className="flex items-baseline gap-2">
                                <span className="text-3xl font-black text-primary">{progress}%</span>
                                <span className="text-muted-foreground text-sm">진행되었습니다</span>
                              </div>
                            </div>
                            <div className="text-right space-y-1">
                              <p className="text-sm text-muted-foreground">모인 금액 / 목표</p>
                              <p className="text-lg font-bold">
                                {campaign.currentAmount.toLocaleString()}원 / {campaign.targetAmount.toLocaleString()}원
                              </p>
                            </div>
                          </div>
                          
                          <Progress value={progress} className="h-4 bg-secondary">
                            <div className="h-full bg-primary rounded-full transition-all duration-1000" style={{ width: `${progress}%` }} />
                          </Progress>
                        </div>

                        <div className="flex flex-col sm:flex-row gap-4 pt-4">
                          <Button 
                            className="w-full rounded-xl h-12 text-lg font-bold"
                            onClick={() => {
                              setSelectedCampaign(campaign)
                              setIsDonationModalOpen(true)
                            }}
                          >
                            후원하기
                          </Button>
                        </div>
                      </CardContent>
                    </Card>
                  )
                })}
              </div>
            ) : (
              <div className="text-center py-20 bg-secondary/10 rounded-3xl border-2 border-dashed border-border">
                <p className="text-muted-foreground">현재 이 보호소에서 진행 중인 캠페인이 없습니다.</p>
              </div>
            )}
          </section>
        </div>
      </main>

      {selectedCampaign && (
        <DonationModal
          isOpen={isDonationModalOpen}
          onClose={() => {
            setIsDonationModalOpen(false)
            setSelectedCampaign(null)
          }}
          campaignTitle={selectedCampaign.title}
          campaignId={selectedCampaign.id}
          onSuccess={() => {
            window.location.reload()
          }}
        />
      )}
    </div>
  )
}

function LandmarkIcon({ className }: { className?: string }) {
  return (
    <svg 
      xmlns="http://www.w3.org/2000/svg" 
      width="24" 
      height="24" 
      viewBox="0 0 24 24" 
      fill="none" 
      stroke="currentColor" 
      strokeWidth="2" 
      strokeLinecap="round" 
      strokeLinejoin="round" 
      className={className}
    >
      <line x1="3" y1="22" x2="21" y2="22"></line>
      <line x1="6" y1="18" x2="6" y2="11"></line>
      <line x1="10" y1="18" x2="10" y2="11"></line>
      <line x1="14" y1="18" x2="14" y2="11"></line>
      <line x1="18" y1="18" x2="18" y2="11"></line>
      <polygon points="12 2 20 7 4 7 12 2"></polygon>
    </svg>
  )
}
