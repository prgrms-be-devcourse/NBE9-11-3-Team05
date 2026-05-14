"use client"

import { useEffect, useState, use } from "react"
import Image from "next/image"
import Link from "next/link"
import { useRouter } from "next/navigation"
import {
  ArrowLeft, Heart, MessageCircle, User, Send, Edit2, Trash2, MoreVertical
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Header } from "@/components/header"
import { useAuth } from "@/lib/auth-context"
import {
  apiRequest, API_ENDPOINTS, toggleFeedLike, deleteFeed, getAnimalDetail,
  type FeedDetail, type FeedComment
} from "@/lib/api"
import { cn, formatDate } from "@/lib/utils"

const CATEGORY_LABELS: Record<string, string> = {
  ADOPTION_REVIEW: "입양후기",
  VOLUNTEER: "봉사활동",
  FREE: "자유게시판",
  PROMOTE: "홍보",
  QUESTION: "질문",
}

export default function FeedDetailPage({ params }: { params: Promise<{ feedId: string }> }) {
  const { feedId } = use(params)
  const router = useRouter()
  const { user } = useAuth()

  const [feed, setFeed] = useState<FeedDetail | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [liked, setLiked] = useState(false)
  const [likeCount, setLikeCount] = useState(0)
  const [comments, setComments] = useState<FeedComment[]>([])

  const [newComment, setNewComment] = useState("")
  const [isSubmittingComment, setIsSubmittingComment] = useState(false)
  const [editingCommentId, setEditingCommentId] = useState<number | null>(null)
  const [editContent, setEditContent] = useState("")
  const [openMenuId, setOpenMenuId] = useState<number | null>(null)
  const [animalInfoLabel, setAnimalInfoLabel] = useState("")

  // ── 피드 로드 ──────────────────────────────────────────────
  useEffect(() => {
    const fetchFeed = async () => {
      setIsLoading(true)
      const { data, error } = await apiRequest<FeedDetail>(
        API_ENDPOINTS.feedDetail(Number(feedId))
      )
      if (error || !data) {
        alert("피드를 불러오지 못했습니다.")
        router.back()
        return
      }
      setFeed(data)
      setLikeCount(data.likeCount)
      setComments(
        [...data.comments].sort(
          (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
        )
      )
      setIsLoading(false)
    }
    fetchFeed()
  }, [feedId])

  useEffect(() => {
    if (!feed?.animalId) {
      setAnimalInfoLabel("")
      return
    }

    const fetchAnimalDetail = async () => {
      const { data, error } = await getAnimalDetail(feed.animalId!)
      if (error || !data) {
        setAnimalInfoLabel("")
        return
      }
      setAnimalInfoLabel(`${data.noticeNo ?? ""} · ${data.kindFillName ?? ""} · ${data.careNm ?? ""}`)
    }

    fetchAnimalDetail()
  }, [feed?.animalId])

  // ── 좋아요 ─────────────────────────────────────────────────
  const handleLike = async () => {
    if (!user) { alert("로그인이 필요합니다"); return }
    const { data, error } = await toggleFeedLike(Number(feedId))
    if (error || !data) return
    setLikeCount(data.likeCount)
    setLiked(data.isLiked)
  }

  // ── 피드 삭제 ──────────────────────────────────────────────
  const handleDeleteFeed = async () => {
    if (!confirm("정말 삭제하시겠어요?")) return
    const { error } = await deleteFeed(Number(feedId))
    if (error) { alert("삭제 실패: " + error); return }
    router.push("/community")
  }

  // ── 댓글 새로고침 ──────────────────────────────────────────
  const refreshComments = async () => {
    const { data } = await apiRequest<{ comments: FeedComment[] }>(
      API_ENDPOINTS.feedComments(Number(feedId))
    )
    if (data?.comments) {
      setComments(
        [...data.comments].sort(
          (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
        )
      )
    }
  }

  // ── 댓글 작성 ──────────────────────────────────────────────
  const handleCommentSubmit = async () => {
    if (!user) { alert("로그인이 필요합니다"); return }
    if (!newComment.trim() || isSubmittingComment) return
    setIsSubmittingComment(true)
    const { error } = await apiRequest(API_ENDPOINTS.feedComments(Number(feedId)), {
      method: "POST",
      body: JSON.stringify({ content: newComment }),
    })
    setIsSubmittingComment(false)
    if (error) { alert("댓글 작성에 실패했습니다."); return }
    setNewComment("")
    await refreshComments()
  }

  // ── 댓글 수정 ──────────────────────────────────────────────
  const handleCommentEditSubmit = async (commentId: number) => {
    if (!editContent.trim()) return
    const { error } = await apiRequest(
      API_ENDPOINTS.feedCommentDetail(Number(feedId), commentId),
      { method: "PATCH", body: JSON.stringify({ content: editContent }) }
    )
    if (error) { alert("댓글 수정에 실패했습니다."); return }
    setEditingCommentId(null)
    await refreshComments()
  }

  // ── 댓글 삭제 ──────────────────────────────────────────────
  const handleCommentDelete = async (commentId: number) => {
    if (!confirm("댓글을 삭제할까요?")) return
    const { error } = await apiRequest(
      API_ENDPOINTS.feedCommentDetail(Number(feedId), commentId),
      { method: "DELETE" }
    )
    if (error) { alert("댓글 삭제에 실패했습니다."); return }
    await refreshComments()
  }

  // ── 로딩 / 에러 ───────────────────────────────────────────
  if (isLoading) {
    return (
      <div className="min-h-screen bg-background">
        <Header />
        <div className="flex items-center justify-center h-[60vh]">
          <div className="text-muted-foreground animate-pulse">로딩 중...</div>
        </div>
      </div>
    )
  }

  if (!feed) return null

  const canEdit = user?.id === feed.userId

  return (
    <div className="min-h-screen bg-background">
      <Header />

      <main className="max-w-2xl mx-auto px-4 py-8">
        {/* 뒤로가기 */}
        <Link
          href="/community"
          className="flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground transition-colors mb-6 w-fit"
        >
          <ArrowLeft className="w-4 h-4" />
          목록으로
        </Link>

        {/* 카드 */}
        <div className="bg-card rounded-2xl shadow-md border border-border overflow-hidden">
          {/* 헤더 */}
          <div className="p-6 pb-0">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-secondary flex items-center justify-center overflow-hidden">
                  {feed.profileImageUrl ? (
                    <Image 
                      src={feed.profileImageUrl} 
                      alt={feed.nickname || "User"} 
                      width={40} 
                      height={40} 
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <User className="w-5 h-5 text-muted-foreground" />
                  )}
                </div>
                <div>
                  <p className="font-semibold text-foreground">{feed.nickname || `${feed.userId}번 사용자`}</p>
                  <p className="text-xs text-muted-foreground">{formatDate(feed.createdAt)}</p>
                </div>
              </div>

              {/* 수정 / 삭제 (본인만) */}
              {canEdit && (
                <div className="flex gap-2">
                  <Link href={`/community/${feed.feedId}/edit`}>
                    <Button variant="ghost" size="sm">수정</Button>
                  </Link>
                  <Button variant="ghost" size="sm" onClick={handleDeleteFeed}>삭제</Button>
                </div>
              )}
            </div>

            {/* 카테고리 배지 */}
            <span className="inline-block px-2 py-0.5 text-xs font-medium rounded-full bg-primary/10 text-primary mb-3">
              {CATEGORY_LABELS[feed.category] ?? feed.category}
            </span>

            {/* 제목 */}
            {feed.animalId && animalInfoLabel && (
              <p className="text-xs text-muted-foreground mb-2">{animalInfoLabel}</p>
            )}
            <h1 className="text-2xl font-bold text-foreground mb-3">{feed.title}</h1>
          </div>

          {/* 이미지 */}
          {feed.imageUrl && (
            <div className="relative aspect-video mx-6 mb-4 rounded-xl overflow-hidden">
              <Image src={feed.imageUrl} alt={feed.title} fill className="object-cover" />
            </div>
          )}

          {/* 본문 */}
          <div className="px-6 pb-4">
            <p className="text-sm text-foreground whitespace-pre-line leading-relaxed">
              {feed.content}
            </p>
          </div>

          {/* 좋아요 / 댓글 수 */}
          <div className="flex items-center gap-4 px-6 py-3 border-t border-border">
            <button
              onClick={handleLike}
              className="flex items-center gap-1.5 text-muted-foreground hover:text-primary transition-colors"
            >
              <Heart className={cn("w-5 h-5", liked && "fill-primary text-primary")} />
              <span className="text-sm font-medium">{likeCount}</span>
            </button>
            <div className="flex items-center gap-1.5 text-muted-foreground">
              <MessageCircle className="w-5 h-5" />
              <span className="text-sm font-medium">{comments.length}</span>
            </div>
          </div>

          {/* 댓글 목록 */}
          <div className="border-t border-border">
            {comments.length === 0 ? (
              <p className="text-center text-sm text-muted-foreground py-8">
                첫 댓글을 남겨보세요!
              </p>
            ) : (
              <ul className="divide-y divide-border">
                {comments.map((comment) => {
                  const isOwner = user?.id === comment.userId
                  const isEditing = editingCommentId === comment.commentId
                  return (
                    <li key={comment.commentId} className="px-6 py-4">
                      <div className="flex items-start justify-between gap-4">
                        <div className="w-8 h-8 rounded-full bg-secondary flex items-center justify-center shrink-0 overflow-hidden mt-1">
                          {comment.profileImageUrl ? (
                            <Image 
                              src={comment.profileImageUrl} 
                              alt={comment.nickname || "User"} 
                              width={32} 
                              height={32} 
                              className="w-full h-full object-cover"
                            />
                          ) : (
                            <User className="w-4 h-4 text-muted-foreground" />
                          )}
                        </div>
                        <div className="flex-1 min-w-0">
                          {/* 작성자 & 날짜 */}
                          <div className="flex items-center gap-2 mb-1 overflow-hidden">
                            <span className="text-sm font-semibold text-foreground truncate">
                              {comment.nickname || "익명"}
                            </span>
                            <span className="text-xs text-muted-foreground shrink-0">
                              {formatDate(comment.createdAt)}
                            </span>
                          </div>

                          {/* 내용 or 수정 입력 */}
                          {isEditing ? (
                            <div className="flex gap-2 mt-1">
                              <Input
                                value={editContent}
                                onChange={(e) => setEditContent(e.target.value)}
                                onKeyDown={(e) => {
                                  if (e.key === "Enter") handleCommentEditSubmit(comment.commentId)
                                  if (e.key === "Escape") setEditingCommentId(null)
                                }}
                                className="h-8 text-sm"
                                autoFocus
                              />
                              <Button size="sm" className="h-8" onClick={() => handleCommentEditSubmit(comment.commentId)}>
                                저장
                              </Button>
                              <Button size="sm" variant="ghost" className="h-8" onClick={() => setEditingCommentId(null)}>
                                취소
                              </Button>
                            </div>
                          ) : (
                            <p className="text-sm text-foreground">{comment.content}</p>
                          )}
                        </div>

                        {/* 수정/삭제 메뉴 (본인만) */}
                        {isOwner && !isEditing && (
                          <div className="relative">
                            <button
                              onClick={() => setOpenMenuId(openMenuId === comment.commentId ? null : comment.commentId)}
                              className="text-muted-foreground hover:text-foreground p-1 rounded-lg hover:bg-secondary transition-colors"
                            >
                              <MoreVertical className="w-4 h-4" />
                            </button>
                            {openMenuId === comment.commentId && (
                              <div className="absolute right-0 top-7 z-10 bg-card border border-border rounded-xl shadow-lg py-1 min-w-[80px]">
                                <button
                                  className="flex items-center gap-2 w-full px-3 py-2 text-sm hover:bg-secondary transition-colors"
                                  onClick={() => {
                                    setEditingCommentId(comment.commentId)
                                    setEditContent(comment.content)
                                    setOpenMenuId(null)
                                  }}
                                >
                                  <Edit2 className="w-3.5 h-3.5" /> 수정
                                </button>
                                <button
                                  className="flex items-center gap-2 w-full px-3 py-2 text-sm text-destructive hover:bg-destructive/10 transition-colors"
                                  onClick={() => {
                                    handleCommentDelete(comment.commentId)
                                    setOpenMenuId(null)
                                  }}
                                >
                                  <Trash2 className="w-3.5 h-3.5" /> 삭제
                                </button>
                              </div>
                            )}
                          </div>
                        )}
                      </div>
                    </li>
                  )
                })}
              </ul>
            )}

            {/* 댓글 입력 */}
            <div className="px-6 py-4 border-t border-border">
              {user ? (
                <div className="flex gap-2">
                  <Input
                    value={newComment}
                    onChange={(e) => setNewComment(e.target.value)}
                    placeholder="댓글을 입력하세요..."
                    className="flex-1"
                    disabled={isSubmittingComment}
                    onKeyDown={(e) => {
                      if (e.key === "Enter" && !e.shiftKey && !e.repeat) {
                        e.preventDefault()
                        handleCommentSubmit()
                      }
                    }}
                  />
                  <Button
                    onClick={handleCommentSubmit}
                    size="icon"
                    className="shrink-0"
                    disabled={!newComment.trim() || isSubmittingComment}
                  >
                    <Send className="w-4 h-4" />
                  </Button>
                </div>
              ) : (
                <p className="text-center text-sm text-muted-foreground py-2">
                  <Link href="/login" className="text-primary hover:underline">로그인</Link> 후 댓글을 작성할 수 있습니다.
                </p>
              )}
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}
