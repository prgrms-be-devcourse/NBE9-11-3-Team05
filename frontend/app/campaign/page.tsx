"use client"

import { useEffect, useState } from "react"
import Link from "next/link"
import { Header } from "@/components/header"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Progress } from "@/components/ui/progress"
import { Button } from "@/components/ui/button"
import { Heart, Landmark, TrendingUp } from "lucide-react"
import { getCampaigns, type Campaign } from "@/lib/api"
import { cn } from "@/lib/utils"
import { DonationModal } from "@/components/donation-modal"

export default function CampaignPage() {
  const [campaigns, setCampaigns] = useState<Campaign[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [isDonationModalOpen, setIsDonationModalOpen] = useState(false)
  const [selectedCampaign, setSelectedCampaign] = useState<Campaign | null>(null)

  useEffect(() => {
    const fetchCampaigns = async () => {
      setIsLoading(true)
      const { data, error } = await getCampaigns()
      
      if (error) {
        setError(error)
      } else if (data) {
        setCampaigns(data.campaigns || [])
      }
      setIsLoading(false)
    }

    fetchCampaigns()
  }, [])

  return (
    <div className="min-h-screen bg-background">
      <Header />
      
      <main className="max-w-6xl mx-auto px-4 py-8 md:py-12">
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-4 mb-10">
          <div>
            <h1 className="text-3xl md:text-4xl font-bold text-foreground mb-3">진행 중인 캠페인</h1>
            <p className="text-muted-foreground text-lg">따뜻한 손길로 아이들의 새로운 시작을 응원해주세요.</p>
          </div>
          <div className="flex items-center gap-2 bg-primary/10 text-primary px-4 py-2 rounded-full text-sm font-semibold self-start">
            <TrendingUp className="w-4 h-4" />
            <span>총 {campaigns.length}개의 캠페인이 진행 중입니다</span>
          </div>
        </div>

        {isLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3].map((i) => (
              <Card key={i} className="animate-pulse border-0 bg-card shadow-sm">
                <div className="h-48 bg-secondary/50 rounded-t-xl" />
                <CardContent className="p-6 space-y-4">
                  <div className="h-6 bg-secondary/50 rounded w-3/4" />
                  <div className="h-4 bg-secondary/50 rounded w-1/2" />
                  <div className="h-2 bg-secondary/50 rounded" />
                </CardContent>
              </Card>
            ))}
          </div>
        ) : error ? (
          <div className="text-center py-20 bg-secondary/20 rounded-3xl border-2 border-dashed border-border">
            <div className="w-16 h-16 bg-destructive/10 text-destructive rounded-2xl flex items-center justify-center mx-auto mb-4">
              <Landmark className="w-8 h-8" />
            </div>
            <h3 className="text-xl font-semibold mb-2">캠페인을 불러오지 못했습니다</h3>
            <p className="text-muted-foreground mb-6">{error}</p>
            <Button onClick={() => window.location.reload()}>다시 시도</Button>
          </div>
        ) : campaigns.length === 0 ? (
          <div className="text-center py-20 bg-secondary/10 rounded-3xl border-2 border-dashed border-border">
            <h3 className="text-xl font-semibold mb-2 text-muted-foreground">현재 진행 중인 캠페인이 없습니다</h3>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {campaigns.map((campaign) => {
              const progress = Math.min(100, Math.round((campaign.currentAmount / campaign.targetAmount) * 100))
              return (
                <Card key={campaign.id} className="group overflow-hidden border-0 bg-card shadow-sm hover:shadow-md transition-all duration-300">
                  <div className="p-6">
                    <div className="flex items-start justify-between mb-4">
                      <div className="w-12 h-12 rounded-xl bg-primary/10 text-primary flex items-center justify-center group-hover:scale-110 transition-transform duration-300">
                        <Heart className="w-6 h-6 fill-current" />
                      </div>
                      <span className={cn(
                        "text-xs font-bold px-2.5 py-1 rounded-full",
                        campaign.status === "ACTIVE" ? "bg-success/10 text-success" : 
                        campaign.status === "COMPLETE" ? "bg-primary/10 text-primary" :
                        "bg-muted text-muted-foreground"
                      )}>
                        {campaign.status === "ACTIVE" ? "진행중" : 
                         campaign.status === "COMPLETE" ? "달성완료" : "종료"}
                      </span>
                    </div>
                    
                    <h3 className="text-xl font-bold mb-2 line-clamp-1 group-hover:text-primary transition-colors">
                      {campaign.title}
                    </h3>
                    <p className="text-sm text-muted-foreground mb-6 leading-relaxed">
                      {campaign.description || "이 캠페인은 보호소의 아이들이 더 좋은 환경에서 지낼 수 있도록 도움을 줍니다."}
                    </p>

                    <div className="space-y-3">
                      <div className="flex justify-between items-end">
                        <div className="space-y-1">
                          <p className="text-xs text-muted-foreground">모인 금액</p>
                          <p className="text-lg font-bold text-foreground">
                            {campaign.currentAmount.toLocaleString()}원
                          </p>
                        </div>
                        <div className="text-right">
                          <p className="text-primary font-bold text-xl">{progress}%</p>
                        </div>
                      </div>
                      <Progress value={progress} className="h-2.5 bg-secondary overflow-hidden">
                        <div className="h-full bg-primary rounded-full transition-all duration-1000" style={{ width: `${progress}%` }} />
                      </Progress>
                      <div className="flex justify-between text-xs text-muted-foreground pt-1">
                        <span>목표: {campaign.targetAmount.toLocaleString()}원</span>
                      </div>
                    </div>
                  </div>
                  
                  <div className="px-6 pb-6 pt-2 flex gap-2">
                    <Link href={`/shelter/${campaign.shelterId}`} className="flex-1">
                      <Button variant="outline" className="w-full rounded-xl bg-secondary/50 hover:bg-secondary text-foreground transition-all duration-300">
                         보호소 정보
                      </Button>
                    </Link>
                    <Button 
                      className="flex-1 rounded-xl bg-primary text-primary-foreground hover:bg-primary/90 transition-all duration-300 shadow-md shadow-primary/10"
                      onClick={() => {
                        setSelectedCampaign(campaign)
                        setIsDonationModalOpen(true)
                      }}
                    >
                      후원하기
                    </Button>
                  </div>
                </Card>
              )
            })}
          </div>
        )}
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
            // Optional: Refresh data
            window.location.reload()
          }}
        />
      )}
    </div>
  )
}
