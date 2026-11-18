package app.pastagram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Blue = Color(0xFF3987F7)
private val Ink = Color(0xFF172235)
private val Muted = Color(0xFF8B97A8)
private data class Chat(val name: String, val message: String, val time: String, val unread: Int, val color: Color)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PastagramApp() }
    }
}

@Composable
fun PastagramApp() {
    var tab by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var draft by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf("Привет! Добро пожаловать в Pastagram 👋", "Это твой новый мессенджер.", "Напиши первое сообщение ниже.") }
    val chats = listOf(
        Chat("Алексей", "Увидимся вечером? 🔥", "23:41", 2, Color(0xFF548DEB)),
        Chat("Команда Pastagram", "Мария: Отличная идея!", "22:18", 5, Color(0xFF8A67D6)),
        Chat("Мама ❤️", "Не забудь позвонить", "Вчера", 0, Color(0xFFE58B9B)),
        Chat("Дизайн и идеи", "Картинка", "Вт", 0, Color(0xFF49A68C)),
        Chat("Денис", "Спасибо!", "Пн", 0, Color(0xFFE1A34B))
    )
    MaterialTheme(colorScheme = lightColorScheme(primary = Blue, background = Color(0xFFF5F7FB), surface = Color.White)) {
        Surface(Modifier.fillMaxSize(), color = Color(0xFFF5F7FB)) {
            if (selected != null) {
                Column(Modifier.fillMaxSize()) {
                    Row(Modifier.fillMaxWidth().background(Color.White).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { selected = null }) { Icon(Icons.Default.ArrowBack, null, tint = Ink) }
                        Avatar(selected!!.first().toString(), Blue)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(selected!!, color = Ink, fontWeight = FontWeight.Bold)
                            Text("был(а) недавно", color = Muted, fontSize = 12.sp)
                        }
                    }
                    LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(messages) { msg ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Surface(color = Color(0xFFDCEBFF), shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)) {
                                    Text(msg, Modifier.padding(12.dp), color = Ink)
                                }
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth().background(Color.White).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(Modifier.weight(1f), color = Color(0xFFF2F4F8), shape = CircleShape) {
                            BasicTextField(value = draft, onValueChange = { draft = it }, modifier = Modifier.padding(14.dp).fillMaxWidth(), singleLine = true, decorationBox = { inner -> if (draft.isEmpty()) Text("Сообщение...", color = Muted); inner() })
                        }
                        IconButton(onClick = { if (draft.isNotBlank()) { messages.add(draft.trim()); draft = "" } }) { Icon(Icons.Default.Send, null, tint = Blue) }
                    }
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    Row(Modifier.fillMaxWidth().background(Color.White).padding(start = 20.dp, end = 12.dp, top = 20.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Pastagram", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, color = Blue)
                            Text(listOf("Твои сообщения", "Все твои контакты", "Твой профиль")[tab], color = Muted, fontSize = 13.sp)
                        }
                        IconButton(onClick = { tab = 2 }) { Icon(Icons.Default.AccountCircle, "Профиль", tint = Ink) }
                    }
                    when (tab) {
                        0 -> {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).background(Color.White, CircleShape).padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Search, null, tint = Muted)
                                Spacer(Modifier.width(8.dp))
                                BasicTextField(query, { query = it }, Modifier.fillMaxWidth(), singleLine = true, decorationBox = { inner -> if (query.isEmpty()) Text("Поиск чатов", color = Muted); inner() })
                            }
                            LazyColumn(Modifier.weight(1f)) {
                                items(chats.filter { it.name.contains(query, true) }) { chat ->
                                    Row(Modifier.fillMaxWidth().clickable { selected = chat.name }.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Avatar(chat.name.first().toString(), chat.color)
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(chat.name, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                            Text(chat.message, color = Muted, fontSize = 13.sp, maxLines = 1)
                                        }
                                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(chat.time, color = Muted, fontSize = 11.sp)
                                            if (chat.unread > 0) Box(Modifier.background(Blue, CircleShape).padding(horizontal = 7.dp, vertical = 3.dp)) { Text(chat.unread.toString(), color = Color.White, fontSize = 10.sp) }
                                        }
                                    }
                                    HorizontalDivider(color = Color(0xFFF0F2F6), modifier = Modifier.padding(start = 78.dp))
                                }
                            }
                        }
                        1 -> Column(Modifier.weight(1f).fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.People, null, tint = Blue, modifier = Modifier.size(54.dp))
                            Text("Контакты", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
                            Text("Здесь появятся твои друзья в Pastagram.", color = Muted)
                        }
                        else -> Column(Modifier.weight(1f).fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(30.dp))
                            Avatar("P", Blue, 88)
                            Spacer(Modifier.height(16.dp))
                            Text("Мой профиль", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
                            Text("@pastagram_user", color = Muted)
                            Spacer(Modifier.height(28.dp))
                            listOf("Уведомления", "Конфиденциальность", "О приложении", "Версия 1.0.0").forEach {
                                Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(14.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text(it, color = Ink) }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                    NavigationBar(containerColor = Color.White) {
                        NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = { Icon(Icons.Default.ChatBubble, null) }, label = { Text("Чаты") })
                        NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, icon = { Icon(Icons.Default.People, null) }, label = { Text("Контакты") })
                        NavigationBarItem(selected = tab == 2, onClick = { tab = 2 }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Профиль") })
                    }
                }
            }
        }
    }
}

@Composable
private fun Avatar(letter: String, color: Color, size: Int = 54) {
    Box(Modifier.size(size.dp).background(color, CircleShape), contentAlignment = Alignment.Center) {
        Text(letter, color = Color.White, fontSize = (size / 2.4).sp, fontWeight = FontWeight.Bold)
    }
}
