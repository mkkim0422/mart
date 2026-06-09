package com.rldjrgo.grocerynote.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.rldjrgo.grocerynote.ui.theme.AppTheme

/**
 * 공유 다이얼로그 placeholder + 빈 입력 fallback 문구 풀. 다이얼로그가 열릴
 * 때마다 이 리스트에서 1개를 무작위로 골라(remember 로 인스턴스마다 고정)
 * 표시하고, 사용자가 입력 없이 보내면 그 문구가 그대로 전송된다.
 *
 * 추가/수정 가이드: 한 줄로 끝나는 부탁 톤만. 너무 길면 placeholder 가
 * 2줄로 잘려 어색해지므로 ~25자 이하 권장.
 */
val SHARE_REQUEST_PLACEHOLDERS: List<String> = listOf(
    "이거 사다 줄래? 🥺",
    "오는 길에 이것 좀 부탁해~",
    "퇴근길에 이거 좀 부탁할게 🙏",
    "마트 가는 김에 이것도 부탁해 🛒",
    "혹시 이거 사다 줄 수 있어?",
    "사다 주면 정말 고마울 텐데 🥹",
    "지나가는 길에 이것만 부탁 🙏",
    "이것 좀 부탁해도 될까? 💕",
    "이거 떨어졌어… 부탁해 🥺",
    "사다 줄 사람~ 손 들어요~ 🙋",
    "오늘 이거 좀 사와 줄래?",
    "장 보는 김에 이거도 부탁 ㅎㅎ",
    "이거 좀 사다 줘! 너밖에 없어 🥲",
    "퇴근 전에 이것만 부탁해 🙇",
    "오늘 저녁용 사다 줄래? 🍚",
)

/**
 * 공유 전 부탁 문구 입력 다이얼로그.
 *
 * 받는 사람(친구·가족)에게 보낼 한 줄 부탁 문구를 위에 얹는다.
 * 빈 입력으로 보내면 이번 다이얼로그 인스턴스에 추첨된 placeholder 문구가
 * 자동 사용됨(SHARE_REQUEST_PLACEHOLDERS 15개 중 무작위 1개) — 본문 텍스트
 * 빌드(buildShareText)는 호출부 책임.
 *
 * 향후 확장 자리 (사용자 요청, 2026-05-26):
 *   - 받는 사람의 OS(Android/iOS)에 따라 분기되는 마트노트 설치 링크 첨부.
 *     마트노트는 현재 Android 전용([[handoff-master]] §1)이라 iOS 분기는
 *     앱 미존재 상태. 분기 자체는 buildShareText 호출부에서 옵션 파라미터로
 *     받게 시그니처를 열어둠. 다이얼로그 본문에 토글/체크박스가 들어갈 자리는
 *     안내 문구 아래 Spacer(12dp) 직후.
 */
@Composable
fun ShareRequestDialog(
    storeName: String,
    storeColor: Color,
    onShare: (note: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    val typo = AppTheme.typography
    var note by remember { mutableStateOf("") }
    // 다이얼로그 인스턴스마다 1개를 추첨해서 고정 — recomposition 중에 무작위가
    // 다시 돌아 placeholder 가 깜빡이는 걸 방지. 다이얼로그 닫고 다시 열면
    // 새로 추첨된다.
    val placeholder = remember { SHARE_REQUEST_PLACEHOLDERS.random() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.bgPrimary,
        title = {
            Text(
                text = "부탁 문구",
                style = typo.headingM,
                color = colors.textPrimary,
            )
        },
        text = {
            Column {
                Text(
                    text = "받는 사람에게 보낼 한 줄 부탁 문구를 적어주세요.\n비워두면 기본 문구로 보내져요.",
                    style = typo.bodyS,
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp)
                        .border(1.5.dp, storeColor, RoundedCornerShape(12.dp))
                        .background(colors.bgSecondary, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.TopStart,
                ) {
                    if (note.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = typo.body,
                            color = colors.textTertiary,
                        )
                    }
                    BasicTextField(
                        value = note,
                        onValueChange = { input ->
                            // 200자 캡 — 한 줄 부탁용이므로 길어지면 본문 가독성 깨짐.
                            note = input.take(200)
                        },
                        textStyle = typo.body.copy(color = colors.textPrimary),
                        cursorBrush = SolidColor(storeColor),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "[$storeName] 장보기 리스트가 함께 전송돼요.",
                    style = typo.bodyS,
                    color = colors.textTertiary,
                )
            }
        },
        confirmButton = {
            Text(
                text = "공유하기",
                style = typo.title,
                color = storeColor,
                modifier = Modifier
                    .clickable {
                        // 빈 입력 → 이번 다이얼로그에 추첨된 placeholder 그대로 전송.
                        // (placeholder 는 시각 안내가 아니라 실제 전송될 기본값.)
                        val resolved = note.trim().ifBlank { placeholder }
                        onShare(resolved)
                    }
                    .padding(12.dp),
            )
        },
        dismissButton = {
            Text(
                text = "취소",
                style = typo.title,
                color = colors.textSecondary,
                modifier = Modifier
                    .clickable(onClick = onDismiss)
                    .padding(12.dp),
            )
        },
    )
}
