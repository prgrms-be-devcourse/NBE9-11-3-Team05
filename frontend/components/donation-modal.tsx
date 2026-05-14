"use client"

import { useState } from "react"
import { X, CreditCard, Heart } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { cn } from "@/lib/utils"

interface DonationModalProps {
  isOpen: boolean
  onClose: () => void
  campaignTitle: string
  campaignId: number
  onSuccess?: () => void
}

export function DonationModal({ isOpen, onClose, campaignTitle, campaignId, onSuccess }: DonationModalProps) {
  const [amount, setAmount] = useState("")
  const [isLoading, setIsLoading] = useState(false)

  if (!isOpen) return null

  const handleDonate = async () => {
    if (!amount || isNaN(Number(amount)) || Number(amount) <= 0) {
      alert("올바른 후원 금액을 입력해주세요.")
      return
    }

    setIsLoading(true)
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    setIsLoading(false)
    alert(`${campaignTitle} 캠페인에 ${Number(amount).toLocaleString()}원이 후원되었습니다. 감사합니다!`)
    onSuccess?.()
    onClose()
  }

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-background/80 backdrop-blur-sm">
      <Card className="w-full max-w-md border-0 shadow-2xl animate-in fade-in zoom-in duration-300">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <div className="flex items-center gap-2">
            <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center text-primary">
              <Heart className="w-5 h-5 fill-current" />
            </div>
            <CardTitle className="text-xl font-bold">후원하기</CardTitle>
          </div>
          <Button variant="ghost" size="icon" onClick={onClose} className="rounded-full">
            <X className="w-5 h-5" />
          </Button>
        </CardHeader>
        
        <CardContent className="pt-6 space-y-6">
          <div className="space-y-4">
            <div>
              <label className="text-sm font-semibold text-muted-foreground mb-1.5 block">캠페인</label>
              <div className="p-3 bg-secondary/50 rounded-xl font-medium text-foreground border border-border">
                {campaignTitle}
              </div>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-semibold text-muted-foreground mb-1.5 block">후원 금액 (원)</label>
              <div className="relative">
                <Input
                  type="text"
                  value={amount}
                  onChange={(e) => {
                    const val = e.target.value.replace(/[^0-9]/g, "")
                    setAmount(val)
                  }}
                  placeholder="금액을 입력하세요"
                  className="pl-4 h-12 rounded-xl text-lg font-bold border-2 focus-visible:ring-primary focus-visible:border-primary transition-all"
                />
                <span className="absolute right-4 top-1/2 -translate-y-1/2 text-muted-foreground font-bold">원</span>
              </div>
              <div className="flex gap-2 pt-1">
                {["10000", "30000", "50000", "100000"].map((preset) => (
                  <Button
                    key={preset}
                    variant="outline"
                    size="sm"
                    className="flex-1 rounded-lg text-xs h-8 border-border bg-card hover:bg-primary/5 hover:border-primary/30 transition-all font-medium"
                    onClick={() => setAmount(preset)}
                  >
                    +{Number(preset).toLocaleString()}
                  </Button>
                ))}
              </div>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-semibold text-muted-foreground mb-1.5 block">결제 수단</label>
              <div className="flex items-center gap-3 p-4 bg-secondary/30 rounded-xl border border-border">
                <CreditCard className="w-5 h-5 text-primary" />
                <span className="font-bold text-foreground">신용 / 체크카드</span>
                <span className="ml-auto text-xs text-muted-foreground">고정</span>
              </div>
            </div>
          </div>

          <div className="pt-4">
            <Button 
              className={cn(
                "w-full h-14 rounded-2xl text-lg font-bold shadow-lg shadow-primary/20 transition-all duration-300",
                isLoading ? "opacity-70" : "hover:scale-[1.02] active:scale-100"
              )}
              onClick={handleDonate}
              disabled={isLoading}
            >
              {isLoading ? (
                <div className="flex items-center gap-2">
                  <div className="w-5 h-5 border-2 border-primary-foreground/30 border-t-primary-foreground rounded-full animate-spin" />
                  <span>처리 중...</span>
                </div>
              ) : (
                <span>{amount ? `${Number(amount).toLocaleString()}원 후원하기` : "금액을 입력해주세요"}</span>
              )}
            </Button>
            <p className="text-center text-[10px] text-muted-foreground mt-4 leading-relaxed">
              * 후원금은 보호소 운영 및 아이들의 치료/간식비로 전액 사용됩니다.<br/>
              결제 시 이용약관 및 개인정보 처리방침에 동의한 것으로 간주됩니다.
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
