"use client"

import { useEffect, useState } from "react"
import Image from "next/image"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { Heart, MessageCircle, User as UserIcon, Send, Plus, X, ImageIcon, Edit2, Trash2 } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardFooter, CardHeader } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Header } from "@/components/header"
import { Pagination } from "@/components/pagination"
import { useAuth } from "@/lib/auth-context"
import { cn, formatDate } from "@/lib/utils"
import { createFeed, updateFeed, FeedPayload, deleteFeed, FeedCategoryFilter, toggleFeedLike, getFeeds, apiRequest, API_ENDPOINTS, getAdoptableAnimals, AdoptedAnimalItem, getAnimalDetail } from "@/lib/api"

interface CommunityComment {
  id: number
  author: string
  authorId: number
  text: string
  createdAt: string
}

type PostCategory = "전체" | "입양후기" | "봉사활동" | "자유게시판"

interface CommunityPost {
  feedId: number
  category: PostCategory
  title: string
  content: string
  nickname: string
  profileImageUrl?: string
  userId: number
  animalId?: number
  imageUrl?: string
  likeCount: number
  isLiked?: boolean
  commentCount: number
  comments: CommunityComment[]
  createdAt: string
}

const POST_CATEGORIES: PostCategory[] = ["전체", "입양후기", "봉사활동", "자유게시판"]

// Mock community data
const mockCommunityPosts: CommunityPost[] = [
  {
    feedId: 1,
    category: "입양후기",
    title: "우리 집 막내 입양 1주년 기념!",
    content: "작년 이맘때 유기동물 보호소에서 우리 막내를 만났어요. 처음엔 무서워서 숨기만 하더니, 이제는 저를 졸졸 따라다녀요. 입양을 고민하시는 분들께, 정말 후회 없는 선택이 될 거예요!",
    nickname: "행복한멍집사",
    userId: 1,
    imageUrl: "https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=800&h=600&fit=crop",
    likeCount: 42,
    commentCount: 2,
    comments: [
      { id: 1, author: "동물사랑", authorId: 2, text: "정말 예쁘네요! 축하드려요", createdAt: "2024-08-10T10:30:00" },
      { id: 2, author: "냥이맘", authorId: 3, text: "1주년 축하해요~ 행복하세요!", createdAt: "2024-08-10T11:00:00" },
    ],
    createdAt: "2024-08-10T09:00:00",
  },
  {
    feedId: 2,
    category: "봉사활동",
    title: "유기견 봉사활동 후기",
    content: "오늘 처음으로 유기동물 보호소에서 봉사활동을 했어요. 아이들이 정말 순하고 사랑스러웠어요. 많은 분들이 관심 가져주시면 좋겠습니다.",
    nickname: "봉사천사",
    userId: 4,
    profileImageUrl: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=100&h=100&fit=crop",
    likeCount: 28,
    commentCount: 1,
    comments: [
      { id: 3, author: "착한마음", authorId: 5, text: "대단하세요! 저도 봉사 신청해야겠어요", createdAt: "2024-08-09T15:00:00" },
    ],
    createdAt: "2024-08-09T14:00:00",
  },
  {
    feedId: 3,
    category: "자유게시판",
    title: "고양이 입양 준비물 공유합니다",
    content: "고양이 입양을 준비하시는 분들을 위해 제가 준비했던 것들 공유할게요.\n\n1. 화장실 + 모래\n2. 사료와 물그릇\n3. 스크래쳐\n4. 캣타워\n5. 장난감\n6. 이동장\n\n처음엔 너무 많은 것 같았지만, 모두 필요했어요!",
    nickname: "고양이초보",
    userId: 6,
    profileImageUrl: "https://images.unsplash.com/photo-1527980965255-d3b416303d12?w=100&h=100&fit=crop",
    imageUrl: "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=800&h=600&fit=crop",
    likeCount: 56,
    commentCount: 3,
    comments: [
      { id: 4, author: "예비집사", authorId: 7, text: "정보 감사합니다! 도움이 많이 됐어요", createdAt: "2024-08-08T20:00:00" },
      { id: 5, author: "냥이천국", authorId: 8, text: "스크래쳐 꼭 필요해요! 가구 긁는 거 방지됩니다", createdAt: "2024-08-08T21:00:00" },
      { id: 6, author: "캣맘", authorId: 9, text: "이동장은 처음부터 있어야 해요. 병원 갈 때 필수!", createdAt: "2024-08-09T09:00:00" },
    ],
    createdAt: "2024-08-08T18:00:00",
  },
  {
    feedId: 4,
    category: "자유게시판",
    title: "우리 동네 길고양이 TNR 했어요",
    content: "드디어 우리 동네 길고양이들 TNR(중성화) 완료했습니다. 구청에서 지원받아서 무료로 진행할 수 있었어요. 관심 있으신 분들은 각 지역 구청에 문의해보세요!",
    nickname: "캣맘연합",
    userId: 10,
    profileImageUrl: "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop",
    likeCount: 34,
    commentCount: 0,
    comments: [],
    createdAt: "2024-08-07T16:00:00",
  },
  {
    feedId: 5,
    category: "자유게시판",
    title: "입양 후 첫 산책 성공!",
    content: "2주 전에 입양한 우리 초코가 드디어 첫 산책을 성공했어요! 처음엔 무서워서 안 나가려고 했는데, 조금씩 적응시키니까 이제 산책을 너무 좋아해요. 인내심이 중요한 것 같아요.",
    nickname: "초코아빠",
    userId: 11,
    profileImageUrl: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&h=100&fit=crop",
    imageUrl: "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=800&h=600&fit=crop",
    likeCount: 45,
    commentCount: 1,
    comments: [
      { id: 7, author: "산책러버", authorId: 12, text: "초코 너무 귀여워요! 첫 산책 축하해요", createdAt: "2024-08-06T12:00:00" },
    ],
    createdAt: "2024-08-06T10:00:00",
  },
]

function CommunityPostCard({
  post,
  onUpdate,
  onDelete
}: {
  post: CommunityPost;
  onUpdate: (updatedPost: CommunityPost) => void
  onDelete: (feedId: number) => void
}) {
  const { user } = useAuth()
  const router = useRouter()
  const canEdit = post.userId === user?.id
  const [showUpdateModal, setShowUpdateModal] = useState(false)
  const [liked, setLiked] = useState(post.isLiked ?? false)
  const [likeCount, setLikeCount] = useState(post.likeCount)
  const [showComments, setShowComments] = useState(false)
  const [newComment, setNewComment] = useState("")
  const [comments, setComments] = useState<any[]>([])
  const [isSubmittingComment, setIsSubmittingComment] = useState(false)
  const [commentCount, setCommentCount] = useState(post.commentCount ?? 0)
  const [hasFetchedComments, setHasFetchedComments] = useState(false)
  const [editingCommentId, setEditingCommentId] = useState<number | null>(null)
  const [editContent, setEditContent] = useState("")
  const [animalInfoLabel, setAnimalInfoLabel] = useState("")

  const fetchComments = async () => {
    const { data } = await apiRequest<{ comments: any[] }>(API_ENDPOINTS.feedComments(post.feedId))
    if (data?.comments) {
      const sorted = data.comments.sort((a: any, b: any) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())
      setComments(sorted)
      setCommentCount(sorted.length)
    } else {
      setComments([])
      setCommentCount(0)
    }
    setHasFetchedComments(true)
  }

  useEffect(() => {
    if (showComments && !hasFetchedComments) {
      fetchComments()
    }
  }, [showComments, hasFetchedComments])

  useEffect(() => {
    if (!post.animalId) {
      setAnimalInfoLabel("")
      return
    }

    const fetchAnimalDetail = async () => {
      const { data, error } = await getAnimalDetail(post.animalId!)
      if (error || !data) {
        setAnimalInfoLabel("")
        return
      }

      setAnimalInfoLabel(`${data.noticeNo ?? ""} · ${data.kindFillName ?? ""} · ${data.careNm ?? ""}`)
    }

    fetchAnimalDetail()
  }, [post.animalId])

  const handleLike = async () => {
    if (!user) {
      alert("로그인이 필요합니다")
      return
    }

    const { data, error } = await toggleFeedLike(post.feedId)

    if (error) {
      alert("좋아요 실패: " + error)
      return
    }

    if (!data) {
      alert("좋아요 응답이 올바르지 않습니다.")
      return
    }

    setLikeCount(data.likeCount)
    setLiked(data.isLiked)
  }

  const handleCommentSubmit = async () => {
    if (!user) {
      alert("로그인이 필요합니다")
      return
    }
    if (!newComment.trim() || isSubmittingComment) return

    setIsSubmittingComment(true)
    const { error } = await apiRequest(API_ENDPOINTS.feedComments(post.feedId), {
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
    const { error } = await apiRequest(API_ENDPOINTS.feedCommentDetail(post.feedId, commentId), {
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
    const { error } = await apiRequest(API_ENDPOINTS.feedCommentDetail(post.feedId, commentId), {
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

  const handleDelete = async () => {
    if (!confirm("정말 삭제하시겠어요?")) return

    const { error } = await deleteFeed(post.feedId)

    if (error) {
      alert("삭제 실패: " + error)
      return
    }

    alert("삭제되었습니다.")
    onDelete(post.feedId)
  }

  return (
    <Card className="border-0 shadow-md bg-card hover:shadow-lg transition-shadow overflow-hidden relative group">
      <CardHeader className="pb-3 relative">
        <div className="flex items-center justify-between w-full">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-secondary flex items-center justify-center overflow-hidden">
              {post.profileImageUrl ? (
                <Image
                  src={post.profileImageUrl}
                  alt={post.nickname}
                  width={40}
                  height={40}
                  className="w-full h-full object-cover"
                />
              ) : (
                <UserIcon className="w-5 h-5 text-muted-foreground" />
              )}
            </div>
            <div>
              <p className="font-semibold text-foreground">{post.nickname}</p>
              <p className="text-xs text-muted-foreground">
                {formatDate(post.createdAt)}
              </p>
            </div>
          </div>
          {canEdit && (
            <div className="flex gap-2 relative z-20">
              <Button variant="ghost" size="sm" onClick={(e) => { e.preventDefault(); e.stopPropagation(); setShowUpdateModal(true) }}>수정</Button>
              <Button variant="ghost" size="sm" onClick={(e) => { e.preventDefault(); e.stopPropagation(); handleDelete() }}>삭제</Button>
            </div>
          )}
        </div>
      </CardHeader>

      <CardContent className="pb-3 space-y-3 relative z-0">
        <div className="flex items-center gap-2">
          <span className="px-2 py-0.5 text-xs font-medium rounded-full bg-primary/10 text-primary">
            {post.category}
          </span>
        </div>
        {showComments && post.animalId && animalInfoLabel && (
          <p className="text-xs text-muted-foreground">{animalInfoLabel}</p>
        )}
        <h3 className="font-bold text-lg text-foreground">{post.title}</h3>
        <p className="text-sm text-muted-foreground whitespace-pre-line leading-relaxed">
          {post.content}
        </p>
        {post.imageUrl && (
          <div className="relative aspect-video rounded-xl overflow-hidden">
            <Image
              src={post.imageUrl}
              alt={post.title}
              fill
              className="object-cover"
            />
          </div>
        )}
      </CardContent>

      <CardFooter className="flex-col items-stretch gap-3 pt-2 relative">
        {/* Action Buttons */}
        <div className="flex items-center gap-4 pb-2 border-b border-border">
          <button
            onClick={(e) => { e.preventDefault(); e.stopPropagation(); handleLike() }}
            className="flex items-center gap-1.5 text-muted-foreground hover:text-primary transition-colors relative z-20"
          >
            <Heart className={cn("w-5 h-5", liked && "fill-primary text-primary")} />
            <span className="text-sm font-medium">{likeCount}</span>
          </button>
          <button
            onClick={(e) => { e.preventDefault(); e.stopPropagation(); setShowComments(!showComments) }}
            className="flex items-center gap-1.5 text-muted-foreground hover:text-foreground transition-colors relative z-20"
          >
            <MessageCircle className="w-5 h-5" />
            <span className="text-sm font-medium">{commentCount}</span>
          </button>
        </div>

        {/* Comments Section */}
        {showComments && (
          <div className="space-y-3 relative z-20">
            {comments.length === 0 ? (
              <p className="text-sm text-muted-foreground text-center py-4">
                첫 번째 댓글을 남겨보세요!
              </p>
            ) : (
              <div className="space-y-2 max-h-48 overflow-y-auto">
                {comments.map((comment) => (
                  <div key={comment.commentId || comment.id} className="flex gap-3 group">
                    <div className="w-8 h-8 rounded-full bg-secondary flex items-center justify-center shrink-0 overflow-hidden">
                      {comment.profileImageUrl ? (
                        <Image
                          src={comment.profileImageUrl}
                          alt="프로필"
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
                          <span className="font-semibold text-foreground text-sm truncate">
                            {comment.nickname || comment.author || "익명"}
                          </span>
                          {comment.createdAt && (
                            <span className="text-xs text-muted-foreground shrink-0">
                              {new Date(comment.createdAt).toLocaleDateString("ko-KR")}
                            </span>
                          )}
                        </div>
                        {user?.id === (comment.userId || comment.authorId) && (
                          <div className="flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                            <button
                              onClick={() => {
                                setEditingCommentId(comment.commentId)
                                setEditContent(comment.content || comment.text || "")
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
                        <p className="text-sm text-muted-foreground mt-1 whitespace-pre-wrap">{comment.content || comment.text}</p>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* Comment Input */}
            <div className="flex gap-2 relative z-20">
              <Input
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter" && !e.repeat) {
                    e.preventDefault()
                    handleCommentSubmit()
                  }
                }}
                placeholder={user ? "댓글을 입력하세요..." : "로그인 후 댓글을 남길 수 있습니다"}
                className="flex-1 rounded-xl bg-secondary/50 border-0 h-10"
                disabled={!user || isSubmittingComment}
              />
              <Button
                onClick={handleCommentSubmit}
                disabled={!user || !newComment.trim() || isSubmittingComment}
                size="icon"
                className="rounded-xl bg-primary text-primary-foreground hover:bg-primary/90"
              >
                <Send className="w-4 h-4" />
              </Button>
            </div>
          </div>
        )}
      </CardFooter>

      {showUpdateModal && (
        <UpdatePostModal
          post={post}
          onClose={() => setShowUpdateModal(false)}
          onSubmit={async (updatedData) => {
            let apiCategory = "FREE";
            if (updatedData.category === "입양후기") apiCategory = "ADOPTION_REVIEW";
            if (updatedData.category === "봉사활동") apiCategory = "VOLUNTEER";

            const payload: FeedPayload = {
              category: apiCategory,
              title: updatedData.title,
              content: updatedData.content,
              imageUrl: updatedData.imageUrl,
              animalId: updatedData.animalId,
            };

            const { data, error } = await updateFeed(post.feedId, payload);

            if (error) {
              alert("피드 수정에 실패했습니다: " + error);
              return;
            }

            onUpdate({
              ...post,
              category: updatedData.category,
              title: updatedData.title,
              content: updatedData.content,
              animalId: updatedData.animalId,
              imageUrl: updatedData.imageUrl,
            });
            setShowUpdateModal(false);
            alert("게시글이 수정되었습니다.");
          }}
        />
      )}
      <Link
        href={`/community/${post.feedId}`}
        className="absolute inset-0 z-10"
        aria-label={`View post: ${post.title}`}
      />
    </Card>
  )
}

function CreatePostModal({ onClose, onSubmit }: { onClose: () => void; onSubmit: (post: Omit<CommunityPost, "feedId" | "createdAt">) => void }) {
  const { user } = useAuth()
  const [category, setCategory] = useState<PostCategory>("자유게시판")
  const [title, setTitle] = useState("")
  const [content, setContent] = useState("")
  const [imageUrl, setImageUrl] = useState("")
  const [animals, setAnimals] = useState<AdoptedAnimalItem[]>([])
  const [selectedAnimalId, setSelectedAnimalId] = useState<number | "">("")
  const [isAnimalsLoading, setIsAnimalsLoading] = useState(false)
  const [hasLoadedAnimals, setHasLoadedAnimals] = useState(false)

  useEffect(() => {
    if (category !== "입양후기" || hasLoadedAnimals) return

    const fetchAnimals = async () => {
      setIsAnimalsLoading(true)
      const { data, error } = await getAdoptableAnimals()
      if (error || !data) {
        console.error("동물 목록 조회 실패:", error)
        setIsAnimalsLoading(false)
        return
      }
      setAnimals(data || [])
      setHasLoadedAnimals(true)
      setIsAnimalsLoading(false)
    }

    fetchAnimals()
  }, [category, hasLoadedAnimals])

  const handleSubmit = () => {
    if (!title.trim() || !content.trim()) {
      alert("제목과 내용을 입력해주세요")
      return
    }
    if (category === "입양후기" && selectedAnimalId === "") {
      alert("입양후기 작성 시 동물 선택은 필수입니다.")
      return
    }

    onSubmit({
      category,
      title,
      content,
      nickname: user?.name || "익명",
      userId: user?.id || 0,
      animalId: category === "입양후기" ? Number(selectedAnimalId) : undefined,
      imageUrl: imageUrl || undefined,
      likeCount: 0,
      commentCount: 0,
      comments: [],
    })
    onClose()
  }

  return (
    <div className="fixed inset-0 bg-foreground/50 flex items-center justify-center z-50 p-4">
      <Card className="w-full max-w-lg border-0 shadow-2xl">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <h2 className="text-lg font-bold text-foreground">새 글 작성</h2>
          <Button variant="ghost" size="icon" onClick={onClose}>
            <X className="w-5 h-5" />
          </Button>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <label className="text-sm font-medium text-foreground">카테고리</label>
            <div className="flex gap-2 flex-wrap">
              {POST_CATEGORIES.filter(c => c !== "전체").map((cat) => (
                <button
                  key={cat}
                  onClick={() => setCategory(cat)}
                  className={cn(
                    "px-3 py-1.5 rounded-lg text-sm font-medium transition-all",
                    category === cat
                      ? "bg-primary text-primary-foreground"
                      : "bg-secondary text-muted-foreground hover:bg-secondary/80"
                  )}
                >
                  {cat}
                </button>
              ))}
            </div>
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium text-foreground">제목</label>
            <Input
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="제목을 입력하세요"
              className="rounded-xl bg-secondary/50 border-0"
            />
          </div>
          {category === "입양후기" && (
            <div className="space-y-2">
              <label className="text-sm font-medium text-foreground">
                동물 선택 <span className="text-destructive">*</span>
              </label>
              <select
                value={selectedAnimalId}
                onChange={(e) => setSelectedAnimalId(e.target.value ? Number(e.target.value) : "")}
                disabled={isAnimalsLoading}
                className={cn(
                  "w-full h-10 px-3 rounded-xl bg-secondary/50 border-0 text-sm",
                  selectedAnimalId === "" && "text-muted-foreground"
                )}
              >
                <option value="">
                  {isAnimalsLoading ? "불러오는 중..." : "동물을 선택하세요"}
                </option>
                {animals.map((a) => (
                  <option key={a.animalId} value={a.animalId}>
                  {a.noticeNo}
                </option>
                ))}
              </select>
            </div>
          )}
          <div className="space-y-2">
            <label className="text-sm font-medium text-foreground">내용</label>
            <Textarea
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="내용을 입력하세요"
              className="rounded-xl bg-secondary/50 border-0 min-h-[200px] max-h-[400px] overflow-y-auto resize-none"
            />
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium text-foreground">이미지 URL (선택)</label>
            <div className="flex gap-2">
              <Input
                value={imageUrl}
                onChange={(e) => setImageUrl(e.target.value)}
                placeholder="https://..."
                className="rounded-xl bg-secondary/50 border-0"
              />
              <Button variant="outline" size="icon" className="shrink-0 rounded-xl">
                <ImageIcon className="w-4 h-4" />
              </Button>
            </div>
          </div>
        </CardContent>
        <CardFooter className="gap-2">
          <Button variant="outline" className="flex-1 rounded-xl" onClick={onClose}>
            취소
          </Button>
          <Button className="flex-1 rounded-xl bg-primary text-primary-foreground hover:bg-primary/90" onClick={handleSubmit}>
            작성하기
          </Button>
        </CardFooter>
      </Card>
    </div>
  )
}

function UpdatePostModal({ post, onClose, onSubmit }: { post: CommunityPost; onClose: () => void; onSubmit: (data: { category: PostCategory, title: string, content: string, imageUrl?: string, animalId?: number }) => void }) {
  const [category, setCategory] = useState<PostCategory>(post.category)
  const [title, setTitle] = useState(post.title)
  const [content, setContent] = useState(post.content)
  const [imageUrl, setImageUrl] = useState(post.imageUrl || "")
  const [animals, setAnimals] = useState<AdoptedAnimalItem[]>([])
  const [selectedAnimalId, setSelectedAnimalId] = useState<number | "">(post.animalId ?? "")
  const [isAnimalsLoading, setIsAnimalsLoading] = useState(false)
  const [hasLoadedAnimals, setHasLoadedAnimals] = useState(false)

  useEffect(() => {
    if (category !== "입양후기" || hasLoadedAnimals) return

    const fetchAnimals = async () => {
      setIsAnimalsLoading(true)
      const { data, error } = await getAdoptableAnimals()
      if (error || !data) {
        console.error("동물 목록 조회 실패:", error)
        setIsAnimalsLoading(false)
        return
      }
      setAnimals(data || [])
      setHasLoadedAnimals(true)
      setIsAnimalsLoading(false)
    }

    fetchAnimals()
  }, [category, hasLoadedAnimals])

  const handleSubmit = () => {
    if (!title.trim() || !content.trim()) {
      alert("제목과 내용을 입력해주세요")
      return
    }
    if (category === "입양후기" && selectedAnimalId === "") {
      alert("입양후기 수정 시 동물 선택은 필수입니다.")
      return
    }

    onSubmit({
      category,
      title,
      content,
      imageUrl: imageUrl || undefined,
      animalId: category === "입양후기" ? Number(selectedAnimalId) : undefined,
    })
  }

  return (
    <div className="fixed inset-0 bg-foreground/50 flex items-center justify-center z-50 p-4">
      <Card className="w-full max-w-lg border-0 shadow-2xl">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <h2 className="text-lg font-bold text-foreground">게시글 수정</h2>
          <Button variant="ghost" size="icon" onClick={onClose}>
            <X className="w-5 h-5" />
          </Button>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <label className="text-sm font-medium text-foreground">카테고리</label>
            <div className="flex gap-2 flex-wrap">
              {POST_CATEGORIES.filter(c => c !== "전체").map((cat) => (
                <button
                  key={cat}
                  onClick={() => setCategory(cat)}
                  className={cn(
                    "px-3 py-1.5 rounded-lg text-sm font-medium transition-all",
                    category === cat
                      ? "bg-primary text-primary-foreground"
                      : "bg-secondary text-muted-foreground hover:bg-secondary/80"
                  )}
                >
                  {cat}
                </button>
              ))}
            </div>
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium text-foreground">제목</label>
            <Input
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="제목을 입력하세요"
              className="rounded-xl bg-secondary/50 border-0"
            />
          </div>
          {category === "입양후기" && (
            <div className="space-y-2">
              <label className="text-sm font-medium text-foreground">
                동물 선택 <span className="text-destructive">*</span>
              </label>
              <select
                value={selectedAnimalId}
                onChange={(e) => setSelectedAnimalId(e.target.value ? Number(e.target.value) : "")}
                disabled={isAnimalsLoading}
                className={cn(
                  "w-full h-10 px-3 rounded-xl bg-secondary/50 border-0 text-sm",
                  selectedAnimalId === "" && "text-muted-foreground"
                )}
              >
                <option value="">
                  {isAnimalsLoading ? "불러오는 중..." : "동물을 선택하세요"}
                </option>
                {animals.map((a) => (
                  <option key={a.animalId} value={a.animalId}>
                  {a.noticeNo}
                </option>
                ))}
              </select>
            </div>
          )}
          <div className="space-y-2">
            <label className="text-sm font-medium text-foreground">내용</label>
            <Textarea
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="내용을 입력하세요"
              className="rounded-xl bg-secondary/50 border-0 min-h-[200px] max-h-[400px] overflow-y-auto resize-none"
            />
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium text-foreground">이미지 URL (선택)</label>
            <div className="flex gap-2">
              <Input
                value={imageUrl}
                onChange={(e) => setImageUrl(e.target.value)}
                placeholder="https://..."
                className="rounded-xl bg-secondary/50 border-0"
              />
              <Button variant="outline" size="icon" className="shrink-0 rounded-xl">
                <ImageIcon className="w-4 h-4" />
              </Button>
            </div>
          </div>
        </CardContent>
        <CardFooter className="gap-2">
          <Button variant="outline" className="flex-1 rounded-xl" onClick={onClose}>
            취소
          </Button>
          <Button className="flex-1 rounded-xl bg-primary text-primary-foreground hover:bg-primary/90" onClick={handleSubmit}>
            수정하기
          </Button>
        </CardFooter>
      </Card>
    </div>
  )
}

export default function CommunityPage() {
  const { user } = useAuth()
  const [posts, setPosts] = useState<CommunityPost[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [currentPage, setCurrentPage] = useState(1)
  const [totalPages, setTotalPages] = useState(1)
  const [selectedCategory, setSelectedCategory] = useState<PostCategory>("전체")
  const postsPerPage = 10

  useEffect(() => {
    const mapCategory = (category?: string): PostCategory => {
      if (category === "ADOPTION_REVIEW" || category === "입양후기") return "입양후기"
      if (category === "VOLUNTEER" || category === "봉사활동") return "봉사활동"
      return "자유게시판"
    }

    const fetchPosts = async () => {
      setIsLoading(true)

      const mapRequestCategory = (category: PostCategory): FeedCategoryFilter | null => {
        if (category === "입양후기") return "ADOPTION_REVIEW"
        if (category === "봉사활동") return "VOLUNTEER"
        if (category === "자유게시판") return "FREE"
        return null;
      }

      const categoryParam = mapRequestCategory(selectedCategory)
      const { data, error } = await getFeeds(currentPage - 1, postsPerPage, categoryParam ?? undefined)

      if (error || !data) {
        console.error("피드 목록 조회 실패, mock 데이터 사용", error)
        setPosts(mockCommunityPosts)
        setIsLoading(false)
        return
      }

      const mappedPosts: CommunityPost[] = (data.content || []).map((post) => ({
        feedId: post.feedId,
        category: mapCategory((post as { category?: string }).category),
        title: post.title,
        content: post.content,
        nickname: post.nickname || "익명",
        profileImageUrl: post.profileImageUrl,
        userId: post.userId,
        animalId: (post as { animalId?: number }).animalId,
        imageUrl: post.imageUrl,
        likeCount: post.likeCount,
        isLiked: (post as any).isLiked ?? false,
        commentCount: post.commentCount || 0,
        comments: [],
        createdAt: post.createdAt,
      }))

      setPosts(mappedPosts)
      setTotalPages(Math.max(data.totalPages || 1, 1))
      setIsLoading(false)
    }

    fetchPosts()
  }, [currentPage, selectedCategory])

  const handleCategoryChange = (category: PostCategory) => {
    setSelectedCategory(category)
    setCurrentPage(1)
  }

  const handleCreatePost = async (newPost: Omit<CommunityPost, "feedId" | "createdAt">) => {
    // API에 맞게 카테고리 매핑
    let apiCategory = "FREE";
    if (newPost.category === "입양후기") apiCategory = "ADOPTION_REVIEW";
    if (newPost.category === "봉사활동") apiCategory = "VOLUNTEER";

    const payload: FeedPayload = {
      category: apiCategory,
      title: newPost.title,
      content: newPost.content,
      imageUrl: newPost.imageUrl,
      animalId: newPost.animalId,
    };

    // 서버로 피드 생성 API 호출
    const { data, error } = await createFeed(payload);

    if (error) {
      alert("피드 생성에 실패했습니다: " + error);
      return;
    }

    // 작성 성공 후 UI 업데이트
    const post: CommunityPost = {
      ...newPost,
      feedId: data?.feedId || Date.now(),
      createdAt: data?.createdAt || new Date().toISOString(),
    }
    setPosts(prev => [post, ...prev])
  }

  const handleUpdatePost = (updatedPost: CommunityPost) => {
    setPosts(prev => prev.map(p => p.feedId === updatedPost.feedId ? updatedPost : p))
  }

  const handleDeletePost = (postId: number) => {
    setPosts(prev => prev.filter(p => p.feedId !== postId))
  }

  return (
    <div className="min-h-screen bg-background">
      <Header />

      <main className="max-w-2xl mx-auto px-4 py-8">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-2xl font-bold text-foreground">커뮤니티</h1>
            <p className="text-sm text-muted-foreground mt-1">
              반려동물 이야기를 나눠보세요
            </p>
          </div>
          <Button
            onClick={() => {
              if (!user) {
                alert("로그인이 필요합니다")
                return
              }
              setShowCreateModal(true)
            }}
            className="gap-2 rounded-xl bg-primary text-primary-foreground hover:bg-primary/90"
          >
            <Plus className="w-4 h-4" />
            글쓰기
          </Button>
        </div>

        {/* Category Filter */}
        <div className="flex gap-2 mb-6 overflow-x-auto pb-2 scrollbar-hide">
          {POST_CATEGORIES.map((category) => (
            <button
              key={category}
              onClick={() => handleCategoryChange(category)}
              className={cn(
                "px-4 py-2 rounded-xl text-sm font-medium whitespace-nowrap transition-all",
                selectedCategory === category
                  ? "bg-primary text-primary-foreground"
                  : "bg-secondary text-muted-foreground hover:bg-secondary/80"
              )}
            >
              {category}
            </button>
          ))}
        </div>

        {/* Posts List */}
        <div className="space-y-6">
          {isLoading && (
            <p className="text-sm text-muted-foreground text-center py-8">게시글을 불러오는 중...</p>
          )}
          {posts.map((post) => (
            <CommunityPostCard
              key={post.feedId}
              post={post}
              onUpdate={handleUpdatePost}
              onDelete={handleDeletePost}
            />
          ))}
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="mt-8">
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={setCurrentPage}
            />
          </div>
        )}
      </main>

      {/* Create Post Modal */}
      {showCreateModal && (
        <CreatePostModal
          onClose={() => setShowCreateModal(false)}
          onSubmit={handleCreatePost}
        />
      )}
    </div>
  )
}
