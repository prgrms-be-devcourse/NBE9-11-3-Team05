"use client"

import { useEffect, useState } from "react"
import Link from "next/link"
import { Heart, MessageCircle, Send, User } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardFooter, CardHeader } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { ImageWithFallback } from "@/components/ui/image-with-fallback"
import { cn } from "@/lib/utils"
import { useAuth } from "@/lib/auth-context"
import { API_ENDPOINTS, apiRequest, normalizeAnimalTemperatureDisplay, parseAddCheerResponse } from "@/lib/api"

interface Comment {
  id: string
  author: string
  authorId?: number
  text: string
}

interface FeedCardProps {
  id: string
  animalId?: number
  processState: "보호중" | "종료(입양)"
  imageUrl: string
  animalInfo: string
  cheerTemperature?: number
  maxCheerTemperature?: number
  totalHeartCount?: number
  adopterDiary?: string
  comments?: Comment[]
  dailyHeartsRemaining?: number
  onCheerSuccess?: (info?: { remainingToday?: number }) => void
}

const MAX_DAILY_HEARTS = 5

export function FeedCard({
  id,
  animalId = 1,
  processState,
  imageUrl,
  animalInfo,
  cheerTemperature = 0,
  maxCheerTemperature = 100,
  totalHeartCount = 0,
  adopterDiary,
  comments: initialComments = [],
  dailyHeartsRemaining = MAX_DAILY_HEARTS,
  onCheerSuccess,
}: FeedCardProps) {
  const { user } = useAuth()
  const [totalHearts, setTotalHearts] = useState(totalHeartCount)
  const [currentTemp, setCurrentTemp] = useState(cheerTemperature)
  const [comment, setComment] = useState("")
  const [comments, setComments] = useState<Comment[]>(initialComments)
  const [showAllComments, setShowAllComments] = useState(false)
  const [isAnimating, setIsAnimating] = useState(false)

  const isProtecting = processState === "보호중"
  const isAdopted = processState === "종료(입양)"
  const progressPercent = (currentTemp / maxCheerTemperature) * 100

  useEffect(() => {
    setCurrentTemp(cheerTemperature)
  }, [cheerTemperature])

  useEffect(() => {
    setTotalHearts(totalHeartCount)
  }, [totalHeartCount])

  const handleCheer = async () => {
    if (!user) {
      alert("로그인이 필요합니다")
      return
    }
    if (dailyHeartsRemaining <= 0) {
      alert("오늘 사용할 수 있는 하트를 모두 사용했습니다. 내일 다시 응원해주세요!")
      return
    }
    if (isProtecting) {
      const { data, error } = await apiRequest<unknown>(API_ENDPOINTS.addCheer(animalId), {
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
      setCurrentTemp(normalizeAnimalTemperatureDisplay(cheer.temperature, maxCheerTemperature))
      setIsAnimating(true)
      setTimeout(() => setIsAnimating(false), 300)
      onCheerSuccess?.({ remainingToday: cheer.remaingCheersToday })
    }
  }

  const handleCommentSubmit = () => {
    if (!user) {
      alert("로그인이 필요합니다")
      return
    }
    if (!comment.trim()) return

    const newComment: Comment = {
      id: `new-${Date.now()}`,
      author: user.name,
      authorId: user.id,
      text: comment,
    }
    setComments(prev => [...prev, newComment])
    setComment("")
  }

  const displayedComments = showAllComments ? comments : comments.slice(0, 2)

  return (
    <Card className="overflow-hidden border-0 shadow-md bg-card">
      {/* Header Badge */}
      <CardHeader className="pb-2 pt-4 px-4">
        {isProtecting ? (
          <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-primary/10 text-primary text-sm font-medium w-fit">
            가족을 찾아요
          </span>
        ) : (
          <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-success/10 text-success text-sm font-medium w-fit">
            입양 완료
          </span>
        )}
      </CardHeader>

      {/* Image - Clickable */}
      <Link href={`/animals/${animalId}`}>
        <div className="relative aspect-square mx-4 rounded-2xl overflow-hidden cursor-pointer group">
          <ImageWithFallback
            src={imageUrl}
            alt={animalInfo}
            fill
            className={cn(
              "object-cover transition-all duration-300 group-hover:scale-105",
              isAdopted && "brightness-90"
            )}
            unoptimized
          />
          {isAdopted && (
            <div className="absolute inset-0 flex items-center justify-center">
              <div className="bg-success/90 text-success-foreground px-6 py-3 rounded-xl rotate-[-12deg] shadow-lg">
                <span className="text-lg font-bold tracking-wide">입양완료</span>
              </div>
            </div>
          )}
        </div>
      </Link>

      <CardContent className="px-4 pt-4 pb-2 space-y-4">
        {/* Animal Info - Clickable */}
        <Link href={`/animals/${animalId}`} className="block">
          <p className="text-sm text-muted-foreground font-medium hover:text-foreground transition-colors">
            {animalInfo}
          </p>
        </Link>

        {/* Cheer Section (only for protecting state) */}
        {isProtecting && (
          <div className="space-y-3">
            {/* Cheer Button - Full Width */}
            <button
              onClick={handleCheer}
              disabled={dailyHeartsRemaining <= 0}
              className={cn(
                "flex items-center justify-center gap-2 w-full py-2 px-4 rounded-xl transition-all",
                dailyHeartsRemaining > 0
                  ? "bg-primary/10 hover:bg-primary/20 text-primary"
                  : "bg-muted text-muted-foreground cursor-not-allowed"
              )}
            >
              <Heart className={cn(
                "w-4 h-4 transition-transform",
                isAnimating && "scale-125 fill-primary"
              )} />
              <span className="text-sm font-medium">응원하기</span>
            </button>

            {/* Temperature Bar */}
            <div className="space-y-1.5">
              <div className="flex items-center justify-between text-sm">
                <span className="font-medium text-foreground">응원 온도</span>
                <span className="text-primary font-semibold">
                  {currentTemp.toFixed(1)}C / {maxCheerTemperature}C
                </span>
              </div>
              <div className="h-2.5 bg-secondary rounded-full overflow-hidden">
                <div
                  className="h-full bg-gradient-to-r from-primary to-accent rounded-full transition-all duration-500 ease-out"
                  style={{ width: `${progressPercent}%` }}
                />
              </div>
            </div>
          </div>
        )}

        {/* Disabled Cheer for Adopted */}
        {isAdopted && (
          <div className="flex items-center justify-center gap-2 py-2 px-4 bg-muted rounded-xl text-muted-foreground">
            <Heart className="w-4 h-4" />
            <span className="text-sm font-medium">응원이 마감되었습니다</span>
          </div>
        )}

        {/* Adopter's Diary (only for adopted state) */}
        {isAdopted && adopterDiary && (
          <div className="bg-success/5 border border-success/20 rounded-xl p-4 space-y-2">
            <h4 className="text-sm font-semibold text-success flex items-center gap-2">
              입양자의 육아일기
            </h4>
            <p className="text-sm text-foreground leading-relaxed">
              {adopterDiary}
            </p>
          </div>
        )}
      </CardContent>

      {/* Comment Section - 메인 동물 목록 댓글 임시 주석 처리
      <CardFooter className="px-4 pb-4 pt-2 flex-col items-stretch gap-3">
        <div className="flex items-center gap-1 text-muted-foreground">
          <MessageCircle className="w-4 h-4" />
          <span className="text-sm">댓글 {comments.length}개</span>
        </div>

        {comments.length > 0 && (
          <div className="space-y-2">
            {displayedComments.map((c) => (
              <div key={c.id} className="flex gap-2">
                <div className="w-6 h-6 rounded-full bg-secondary flex items-center justify-center shrink-0">
                  <User className="w-3 h-3 text-muted-foreground" />
                </div>
                <div className="flex-1 min-w-0">
                  <span className="text-sm">
                    <span className="font-semibold text-foreground">{c.author}</span>{" "}
                    <span className="text-muted-foreground">{c.text}</span>
                  </span>
                </div>
              </div>
            ))}
            {comments.length > 2 && !showAllComments && (
              <button 
                onClick={() => setShowAllComments(true)}
                className="text-sm text-muted-foreground hover:text-foreground transition-colors"
              >
                댓글 {comments.length}개 모두 보기
              </button>
            )}
          </div>
        )}

        <div className="flex gap-2">
          <div className="relative flex-1">
            <Input
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && handleCommentSubmit()}
              placeholder={
                user
                  ? isAdopted
                    ? "입양 축하 댓글을 남겨주세요!"
                    : "응원 댓글을 남겨주세요..."
                  : "로그인 후 댓글을 남길 수 있습니다"
              }
              className="pr-10 rounded-xl bg-secondary/50 border-0 h-10 placeholder:text-muted-foreground/60"
              disabled={!user}
            />
            <button
              onClick={handleCommentSubmit}
              className={cn(
                "absolute right-2 top-1/2 -translate-y-1/2 p-1.5 rounded-full transition-colors",
                comment && user
                  ? "text-primary hover:bg-primary/10"
                  : "text-muted-foreground/40"
              )}
              disabled={!comment || !user}
            >
              <Send className="w-4 h-4" />
            </button>
          </div>
        </div>
      </CardFooter>
      */}
    </Card>
  )
}
