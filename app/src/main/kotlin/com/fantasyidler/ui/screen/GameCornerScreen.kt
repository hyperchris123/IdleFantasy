package com.fantasyidler.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fantasyidler.R
import com.fantasyidler.ui.theme.GoldPrimary
import com.fantasyidler.ui.viewmodel.BlackjackPhase
import com.fantasyidler.ui.viewmodel.BlackjackResult
import com.fantasyidler.ui.viewmodel.BlackjackState
import com.fantasyidler.ui.viewmodel.CardFlipState
import com.fantasyidler.ui.viewmodel.DicePhase
import com.fantasyidler.ui.viewmodel.DiceState
import com.fantasyidler.ui.viewmodel.FlipCard
import com.fantasyidler.ui.viewmodel.GameCornerViewModel
import com.fantasyidler.ui.viewmodel.GameTab
import com.fantasyidler.ui.viewmodel.PlayingCard
import com.fantasyidler.ui.viewmodel.PokerHand
import com.fantasyidler.ui.viewmodel.RouletteBetType
import com.fantasyidler.ui.viewmodel.RouletteColor
import com.fantasyidler.ui.viewmodel.RouletteState
import com.fantasyidler.ui.viewmodel.ScratchCardState
import com.fantasyidler.ui.viewmodel.ScratchSymbol
import com.fantasyidler.ui.viewmodel.SlotsState
import com.fantasyidler.ui.viewmodel.VideoPokerPhase
import com.fantasyidler.ui.viewmodel.VideoPokerState
import com.fantasyidler.ui.viewmodel.evaluatePokerHand
import com.fantasyidler.ui.viewmodel.handValue
import com.fantasyidler.ui.viewmodel.rouletteColor
import com.fantasyidler.util.formatCoins
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameCornerScreen(
    onBack: () -> Unit = {},
    viewModel: GameCornerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val tabs = listOf(
        stringResource(R.string.game_slots_title),
        stringResource(R.string.game_dice_title),
        stringResource(R.string.game_lottery_title),
        stringResource(R.string.game_cards_title),
        stringResource(R.string.game_blackjack_title),
        stringResource(R.string.game_roulette_title),
        stringResource(R.string.game_scratch_title),
        stringResource(R.string.game_video_poker_title),
    )
    val tabValues = GameTab.values()
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    LaunchedEffect(pagerState.currentPage) {
        viewModel.selectTab(tabValues[pagerState.currentPage])
    }

    LaunchedEffect(state.snackbarMessage) {
        val msg = state.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    LaunchedEffect(state.rareDropGem) {
        val item = state.rareDropGem ?: return@LaunchedEffect
        val name = when (item) {
            "ring_of_fortune"    -> "Ring of Fortune"
            "amulet_of_fortune"  -> "Amulet of Fortune"
            else -> item.replace('_', ' ').replaceFirstChar { it.uppercase() }
        }
        snackbarHostState.showSnackbar("Rare drop! You found a $name!")
        viewModel.clearSnackbar()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_corner_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text  = "Coins: ${state.coins.formatCoins()}",
                    style = MaterialTheme.typography.titleSmall,
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }

            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding      = 0.dp,
            ) {
                tabs.forEachIndexed { index, label ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick  = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text     = { Text(label, fontSize = 13.sp) },
                    )
                }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                when (tabValues[page]) {
                    GameTab.SLOTS      -> SlotsTab(state.slotsState, state.coins, viewModel)
                    GameTab.HIGH_LOW   -> DiceTab(state.diceState, state.coins, viewModel)
                    GameTab.LOTTERY    -> LotteryTab(
                        tickets       = state.lotteryTickets,
                        lastDrawAt    = state.lotteryLastDrawAt,
                        buyQty        = state.lotteryBuyQty,
                        drawResult    = state.lotteryDrawResult,
                        coins         = state.coins,
                        viewModel     = viewModel,
                    )
                    GameTab.CARD_FLIP   -> CardFlipTab(state.cardFlipState, state.coins, viewModel)
                    GameTab.BLACKJACK   -> BlackjackTab(state.blackjackState, state.coins, viewModel)
                    GameTab.ROULETTE    -> RouletteTab(state.rouletteState, state.coins, viewModel)
                    GameTab.SCRATCH_CARD -> ScratchCardTab(state.scratchCardState, state.coins, viewModel)
                    GameTab.VIDEO_POKER -> VideoPokerTab(state.videoPokerState, state.coins, viewModel)
                }
            }
        }
    }
}

// ── Slots ────────────────────────────────────────────────────────────────────

@Composable
private fun SlotsTab(
    slots: SlotsState,
    coins: Long,
    viewModel: GameCornerViewModel,
) {
    Column(
        modifier              = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        Row(
            modifier             = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            slots.reels.forEach { sym ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(80.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(sym.display, fontSize = 32.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        slots.lastWon?.let { won ->
            Text(
                text      = "Won ${won.formatCoins()} coins!",
                color     = GoldPrimary,
                fontWeight = FontWeight.Bold,
                style     = MaterialTheme.typography.titleMedium,
            )
        }

        HorizontalDivider()

        BetRow(
            label       = stringResource(R.string.game_bet_label, slots.betAmount.formatCoins()),
            bet         = slots.betAmount,
            minBet      = 100L,
            step        = 100L,
            coins       = coins,
            onDecrease  = { viewModel.slotsSetBet(slots.betAmount - 100L) },
            onIncrease  = { viewModel.slotsSetBet(slots.betAmount + 100L) },
        )

        Button(
            onClick  = { viewModel.spinSlots() },
            enabled  = coins >= slots.betAmount,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.game_spin))
        }

        PayoutTable()
    }
}

@Composable
private fun PayoutTable() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Payouts (3 of a kind)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            listOf("🍒 Cherry" to "2x", "🍋 Lemon" to "3x", "🍊 Orange" to "4x",
                   "🔔 Bell" to "8x", "BAR" to "10x", "7️⃣ Seven" to "20x", "💎 Diamond" to "50x"
            ).forEach { (sym, pay) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(sym, style = MaterialTheme.typography.bodySmall)
                    Text(pay, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = GoldPrimary)
                }
            }
        }
    }
}

// ── Dice / High-Low ──────────────────────────────────────────────────────────

@Composable
private fun DiceTab(
    dice: DiceState,
    coins: Long,
    viewModel: GameCornerViewModel,
) {
    Column(
        modifier              = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        when (dice.phase) {
            DicePhase.BETTING -> {
                Text("Guess if the next roll will be higher or lower!", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)

                BetRow(
                    label       = stringResource(R.string.game_bet_label, dice.betAmount.formatCoins()),
                    bet         = dice.betAmount,
                    minBet      = 100L,
                    step        = 100L,
                    coins       = coins,
                    onDecrease  = { viewModel.diceSetBet(dice.betAmount - 100L) },
                    onIncrease  = { viewModel.diceSetBet(dice.betAmount + 100L) },
                )

                Button(
                    onClick  = { viewModel.diceRoll() },
                    enabled  = coins >= dice.betAmount,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Roll")
                }
            }

            DicePhase.PLAYER_CHOICE -> {
                DiceFace(dice.currentRoll)
                Text("Current roll: ${dice.currentRoll}", style = MaterialTheme.typography.titleMedium)
                Text("Pot: ${dice.pendingPot.formatCoins()} coins", color = GoldPrimary, fontWeight = FontWeight.Bold)
                Text("Guess the next roll:", style = MaterialTheme.typography.bodyMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { viewModel.diceChoose(true) }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.game_higher))
                    }
                    Button(onClick = { viewModel.diceChoose(false) }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.game_lower))
                    }
                }
            }

            DicePhase.RESULT_WIN -> {
                DiceFace(dice.nextRoll)
                Text("Rolled ${dice.nextRoll}!", style = MaterialTheme.typography.titleMedium)
                Text("Pot: ${dice.pendingPot.formatCoins()} coins", color = GoldPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { viewModel.diceRollAgain() }, modifier = Modifier.weight(1f)) {
                        Text("Roll Again")
                    }
                    Button(onClick = { viewModel.diceCashOut() }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.game_cash_out))
                    }
                }
            }

            DicePhase.RESULT_PUSH -> {
                DiceFace(dice.nextRoll)
                Text("Rolled ${dice.nextRoll} — Push! Bet returned.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                Button(onClick = { viewModel.diceReset() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Play Again")
                }
            }

            DicePhase.RESULT_LOSE -> {
                DiceFace(dice.nextRoll)
                Text("Rolled ${dice.nextRoll} — you lose!", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
                Button(onClick = { viewModel.diceReset() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Play Again")
                }
            }
        }

        if (dice.phase == DicePhase.BETTING) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Rules", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text("• Win: 1.8x your pot", style = MaterialTheme.typography.bodySmall)
                    Text("• Tie: push (pot returned)", style = MaterialTheme.typography.bodySmall)
                    Text("• Chain wins to grow your pot!", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun DiceFace(value: Int) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(96.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text      = when (value) {
                    1 -> "⚀"; 2 -> "⚁"; 3 -> "⚂"; 4 -> "⚃"; 5 -> "⚄"; else -> "⚅"
                },
                fontSize  = 48.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Lottery ──────────────────────────────────────────────────────────────────

@Composable
private fun LotteryTab(
    tickets: Int,
    lastDrawAt: Long,
    buyQty: Int,
    drawResult: String?,
    coins: Long,
    viewModel: GameCornerViewModel,
) {
    val msUntilDraw = if (lastDrawAt == 0L) 0L else {
        val next = lastDrawAt + TimeUnit.HOURS.toMillis(24)
        (next - System.currentTimeMillis()).coerceAtLeast(0L)
    }
    val canDraw = msUntilDraw == 0L && tickets > 0

    Column(
        modifier              = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("🎟️", fontSize = 40.sp)
                Text("Tickets held: $tickets / 20", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (lastDrawAt > 0L && msUntilDraw > 0L) {
                    val h = TimeUnit.MILLISECONDS.toHours(msUntilDraw)
                    val m = TimeUnit.MILLISECONDS.toMinutes(msUntilDraw) % 60
                    Text(stringResource(R.string.game_next_draw, "${h}h ${m}m"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else if (tickets > 0) {
                    Text("Ready to draw!", style = MaterialTheme.typography.bodySmall, color = GoldPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (drawResult != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth().clickable { viewModel.lotteryClearResult() },
            ) {
                Text(
                    text      = drawResult,
                    modifier  = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    style     = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        HorizontalDivider()

        Text("Buy Tickets (1,000 coins each)", style = MaterialTheme.typography.labelLarge)

        BetRow(
            label       = "$buyQty ticket${if (buyQty == 1) "" else "s"} = ${(buyQty * 1_000L).formatCoins()} coins",
            bet         = buyQty * 1_000L,
            minBet      = 1_000L,
            step        = 1_000L,
            coins       = coins,
            onDecrease  = { viewModel.lotterySetBuyQty(buyQty - 1) },
            onIncrease  = { viewModel.lotterySetBuyQty(buyQty + 1) },
        )

        Button(
            onClick  = { viewModel.lotteryBuyTickets() },
            enabled  = coins >= buyQty * 1_000L && tickets < 20,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.game_buy_tickets))
        }

        Button(
            onClick  = { viewModel.lotteryDraw() },
            enabled  = canDraw,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.game_draw_lottery))
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Prize odds (per ticket)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text("• 2% chance — 20,000 coins", style = MaterialTheme.typography.bodySmall)
                Text("• 8% chance — 5,000 coins", style = MaterialTheme.typography.bodySmall)
                Text("• 15% chance — 1,500 coins", style = MaterialTheme.typography.bodySmall)
                Text("• 75% chance — no win", style = MaterialTheme.typography.bodySmall)
                Text("Draw available every 24 hours.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── Card Flip ────────────────────────────────────────────────────────────────

@Composable
private fun CardFlipTab(
    flip: CardFlipState,
    coins: Long,
    viewModel: GameCornerViewModel,
) {
    Column(
        modifier              = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        if (flip.betting && flip.cards.isEmpty()) {
            Text("Find the Ace! Pick the right card to win 2.5x your bet.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)

            BetRow(
                label       = stringResource(R.string.game_bet_label, flip.betAmount.formatCoins()),
                bet         = flip.betAmount,
                minBet      = 100L,
                step        = 100L,
                coins       = coins,
                onDecrease  = { viewModel.cardFlipSetBet(flip.betAmount - 100L) },
                onIncrease  = { viewModel.cardFlipSetBet(flip.betAmount + 100L) },
            )

            Button(
                onClick  = { viewModel.cardFlipStart() },
                enabled  = coins >= flip.betAmount,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Flip Cards")
            }
        } else {
            flip.lastWon?.let { won ->
                Text(
                    text       = "You found the Ace! Won ${won.formatCoins()} coins!",
                    color      = GoldPrimary,
                    fontWeight = FontWeight.Bold,
                    style      = MaterialTheme.typography.titleMedium,
                    textAlign  = TextAlign.Center,
                )
            } ?: run {
                if (flip.cards.any { it.revealed }) {
                    Text("Not the Ace! Better luck next time.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
                }
            }

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                flip.cards.forEachIndexed { i, card ->
                    FlipCardItem(
                        card     = card,
                        onClick  = { if (!flip.cards.any { it.revealed }) viewModel.cardFlipPick(i) },
                    )
                }
            }

            if (flip.cards.any { it.revealed }) {
                Button(
                    onClick  = { viewModel.cardFlipStart() },
                    enabled  = coins >= flip.betAmount,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Play Again")
                }
                BetRow(
                    label       = stringResource(R.string.game_bet_label, flip.betAmount.formatCoins()),
                    bet         = flip.betAmount,
                    minBet      = 100L,
                    step        = 100L,
                    coins       = coins,
                    onDecrease  = { viewModel.cardFlipSetBet(flip.betAmount - 100L) },
                    onIncrease  = { viewModel.cardFlipSetBet(flip.betAmount + 100L) },
                )
            }
        }
    }
}

@Composable
private fun FlipCardItem(card: FlipCard, onClick: () -> Unit) {
    val bgColor = when {
        !card.revealed -> MaterialTheme.colorScheme.primary
        card.isAce     -> GoldPrimary
        else           -> MaterialTheme.colorScheme.surfaceVariant
    }
    Surface(
        shape    = RoundedCornerShape(8.dp),
        color    = bgColor,
        modifier = Modifier.size(80.dp).clickable(enabled = !card.revealed, onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text      = if (!card.revealed) "?" else if (card.isAce) "A♠" else "?",
                fontSize  = 24.sp,
                textAlign = TextAlign.Center,
                color     = if (card.revealed && card.isAce) Color.Black else MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ── Blackjack ─────────────────────────────────────────────────────────────────

@Composable
private fun BlackjackTab(
    bj: BlackjackState,
    coins: Long,
    viewModel: GameCornerViewModel,
) {
    Column(
        modifier              = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        when (bj.phase) {
            BlackjackPhase.BETTING -> {
                Text("Beat the dealer! Get closer to 21 without going over.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)

                BetRow(
                    label       = stringResource(R.string.game_bet_label, bj.betAmount.formatCoins()),
                    bet         = bj.betAmount,
                    minBet      = 100L,
                    step        = 100L,
                    coins       = coins,
                    onDecrease  = { viewModel.blackjackSetBet(bj.betAmount - 100L) },
                    onIncrease  = { viewModel.blackjackSetBet(bj.betAmount + 100L) },
                )

                Button(
                    onClick  = { viewModel.blackjackDeal() },
                    enabled  = coins >= bj.betAmount,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.game_deal))
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Rules", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text("• Blackjack pays 2.5x", style = MaterialTheme.typography.bodySmall)
                        Text("• Win pays 2x", style = MaterialTheme.typography.bodySmall)
                        Text("• Push returns your bet", style = MaterialTheme.typography.bodySmall)
                        Text("• Dealer hits on 16 or less", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            BlackjackPhase.PLAYER_TURN -> {
                BlackjackHands(bj, showDealerHole = false)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.blackjackHit() }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.game_hit))
                    }
                    Button(onClick = { viewModel.blackjackStand() }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.game_stand))
                    }
                    if (bj.playerHand.size == 2 && coins >= bj.betAmount) {
                        Button(
                            onClick = { viewModel.blackjackDoubleDown() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        ) {
                            Text(stringResource(R.string.game_double_down), color = Color.Black, fontSize = 10.sp)
                        }
                    }
                }
            }

            BlackjackPhase.DEALER_TURN, BlackjackPhase.RESULT -> {
                BlackjackHands(bj, showDealerHole = true)
                val resultText = when (bj.result) {
                    BlackjackResult.BLACKJACK -> "Blackjack! Won ${bj.payout.formatCoins()} coins!"
                    BlackjackResult.WIN       -> "You win! Won ${bj.payout.formatCoins()} coins!"
                    BlackjackResult.PUSH      -> stringResource(R.string.game_push)
                    BlackjackResult.BUST      -> "Bust! You went over 21."
                    BlackjackResult.LOSE      -> "Dealer wins."
                    null                      -> ""
                }
                val resultColor = when (bj.result) {
                    BlackjackResult.BLACKJACK, BlackjackResult.WIN -> GoldPrimary
                    BlackjackResult.BUST, BlackjackResult.LOSE     -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
                Text(resultText, style = MaterialTheme.typography.titleMedium, color = resultColor, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                Button(onClick = { viewModel.blackjackReset() }, modifier = Modifier.fillMaxWidth()) {
                    Text("New Hand")
                }
            }
        }
    }
}

@Composable
private fun BlackjackHands(bj: BlackjackState, showDealerHole: Boolean) {
    val dealerVal = if (showDealerHole) handValue(bj.dealerHand) else handValue(bj.dealerHand.take(1))
    val playerVal = handValue(bj.playerHand)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text("Dealer (${if (showDealerHole) dealerVal else "?"})", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            bj.dealerHand.forEachIndexed { i, card ->
                CardChip(card = card, faceDown = !showDealerHole && i == 1)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("You ($playerVal)", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            bj.playerHand.forEach { card -> CardChip(card) }
        }
    }
}

@Composable
private fun CardChip(card: PlayingCard, faceDown: Boolean = false) {
    val isRed = card.suit == 1 || card.suit == 2
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (faceDown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .size(width = 44.dp, height = 60.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (faceDown) {
                Text("?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(card.display, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isRed) Color.Red else MaterialTheme.colorScheme.onSurface)
                    Text(card.suitSymbol, fontSize = 12.sp, color = if (isRed) Color.Red else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

// ── Roulette ──────────────────────────────────────────────────────────────────

@Composable
private fun RouletteTab(rs: RouletteState, coins: Long, viewModel: GameCornerViewModel) {
    val betTypes = listOf(
        RouletteBetType.RED to "Red", RouletteBetType.BLACK to "Black",
        RouletteBetType.ODD to "Odd", RouletteBetType.EVEN to "Even",
        RouletteBetType.DOZEN_1 to "1-12", RouletteBetType.DOZEN_2 to "13-24",
        RouletteBetType.DOZEN_3 to "25-36", RouletteBetType.NUMBER to "Number",
    )
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        rs.result?.let { n ->
            val color = rouletteColor(n)
            val bgColor = when (color) {
                RouletteColor.RED   -> Color(0xFFB71C1C)
                RouletteColor.BLACK -> Color(0xFF212121)
                RouletteColor.GREEN -> Color(0xFF1B5E20)
            }
            Surface(shape = RoundedCornerShape(50), color = bgColor, modifier = Modifier.size(96.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("$n", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            rs.lastWon?.let { won ->
                Text("Won ${won.formatCoins()} coins!", color = GoldPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        } ?: Box(
            modifier = Modifier.size(96.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text("?", fontSize = 36.sp, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider()
        Text("Select bet type:", style = MaterialTheme.typography.labelMedium)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            betTypes.chunked(4).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { (type, label) ->
                        val selected = rs.betType == type
                        val btnColor = when {
                            selected && type == RouletteBetType.RED   -> Color(0xFFB71C1C)
                            selected && type == RouletteBetType.BLACK -> Color(0xFF212121)
                            selected -> MaterialTheme.colorScheme.primary
                            else     -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        Surface(
                            shape    = RoundedCornerShape(8.dp),
                            color    = btnColor,
                            modifier = Modifier.weight(1f).clickable { viewModel.rouletteSetBetType(type) },
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                Text(label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        if (rs.betType == RouletteBetType.NUMBER) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { viewModel.rouletteSetNumber(rs.selectedNumber - 1) }, enabled = rs.selectedNumber > 0, modifier = Modifier.size(40.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) { Text("-") }
                Text("Number: ${rs.selectedNumber}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = { viewModel.rouletteSetNumber(rs.selectedNumber + 1) }, enabled = rs.selectedNumber < 36, modifier = Modifier.size(40.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) { Text("+") }
            }
        }

        BetRow(
            label      = stringResource(R.string.game_bet_label, rs.betAmount.formatCoins()),
            bet        = rs.betAmount, minBet = 100L, step = 100L, coins = coins,
            onDecrease = { viewModel.rouletteSetBet(rs.betAmount - 100L) },
            onIncrease = { viewModel.rouletteSetBet(rs.betAmount + 100L) },
        )

        Button(onClick = { viewModel.rouletteSpin() }, enabled = coins >= rs.betAmount, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.game_spin_wheel))
        }

        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Payouts", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                listOf("Red / Black" to "2x", "Odd / Even" to "2x", "Dozen (1-12 etc.)" to "3x", "Single Number" to "36x").forEach { (bet, pay) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(bet, style = MaterialTheme.typography.bodySmall)
                        Text(pay, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = GoldPrimary)
                    }
                }
            }
        }
    }
}

// ── Scratch Card ──────────────────────────────────────────────────────────────

@Composable
private fun ScratchCardTab(sc: ScratchCardState, coins: Long, viewModel: GameCornerViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        if (sc.cells.isEmpty()) {
            Text("Match 3 symbols in any row to win!", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            Text("Price: ${sc.price.formatCoins()} coins", style = MaterialTheme.typography.titleMedium, color = GoldPrimary, fontWeight = FontWeight.Bold)
            Button(onClick = { viewModel.scratchBuyCard() }, enabled = coins >= sc.price, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.game_buy_card))
            }
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Payouts (row of 3)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    ScratchSymbol.values().forEach { sym ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${sym.display} ${sym.name.lowercase().replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.bodySmall)
                            Text("${sym.multiplier}x price", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = GoldPrimary)
                        }
                    }
                }
            }
        } else {
            val allRevealed = sc.cells.all { it.revealed }
            sc.won?.let { won ->
                if (won > 0) Text("You won ${won.formatCoins()} coins!", color = GoldPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                else Text("No winning rows this time.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (row in 0..2) {
                    val rowCells = sc.cells.subList(row * 3, row * 3 + 3)
                    val isWinRow = rowCells.all { it.revealed } && rowCells[0].symbol == rowCells[1].symbol && rowCells[1].symbol == rowCells[2].symbol
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        rowCells.forEachIndexed { col, cell ->
                            val cellIndex = row * 3 + col
                            Surface(
                                shape    = RoundedCornerShape(8.dp),
                                color    = if (isWinRow) GoldPrimary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f).height(72.dp).clickable(enabled = !cell.revealed) { viewModel.scratchRevealCell(cellIndex) },
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (cell.revealed) Text(cell.symbol.display, fontSize = 28.sp)
                                    else Text("?", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            if (!allRevealed) {
                Button(onClick = { viewModel.scratchRevealAll() }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.game_reveal_all))
                }
            } else {
                Button(onClick = { viewModel.scratchReset() }, enabled = coins >= sc.price, modifier = Modifier.fillMaxWidth()) {
                    Text("New Card (${sc.price.formatCoins()} coins)")
                }
            }
        }
    }
}

// ── Video Poker ───────────────────────────────────────────────────────────────

@Composable
private fun VideoPokerTab(vp: VideoPokerState, coins: Long, viewModel: GameCornerViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        when (vp.phase) {
            VideoPokerPhase.BETTING -> {
                Text("Jacks or Better — hold the cards you want to keep, then draw.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                BetRow(
                    label = stringResource(R.string.game_bet_label, vp.betAmount.formatCoins()),
                    bet = vp.betAmount, minBet = 100L, step = 100L, coins = coins,
                    onDecrease = { viewModel.videoPokerSetBet(vp.betAmount - 100L) },
                    onIncrease = { viewModel.videoPokerSetBet(vp.betAmount + 100L) },
                )
                Button(onClick = { viewModel.videoPokerDeal() }, enabled = coins >= vp.betAmount, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.game_deal))
                }
                PokerPayTable(vp.betAmount)
            }

            VideoPokerPhase.HOLDING -> {
                Text("Tap cards to hold. Held cards are kept on the draw.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                PokerHandRow(vp.hand, vp.held, onToggle = { viewModel.videoPokerToggleHold(it) })
                Button(onClick = { viewModel.videoPokerDraw() }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.game_draw_cards))
                }
            }

            VideoPokerPhase.RESULT -> {
                val resultColor = if (vp.payout > 0) GoldPrimary else MaterialTheme.colorScheme.error
                val resultText = if (vp.result != null)
                    "${vp.result.label}! Won ${vp.payout.formatCoins()} coins!"
                else
                    "No winning hand."
                Text(resultText, style = MaterialTheme.typography.titleMedium, color = resultColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                PokerHandRow(vp.hand, List(5) { false }, onToggle = {})
                Button(onClick = { viewModel.videoPokerReset() }, modifier = Modifier.fillMaxWidth()) {
                    Text("New Hand")
                }
                BetRow(
                    label = stringResource(R.string.game_bet_label, vp.betAmount.formatCoins()),
                    bet = vp.betAmount, minBet = 100L, step = 100L, coins = coins,
                    onDecrease = { viewModel.videoPokerSetBet(vp.betAmount - 100L) },
                    onIncrease = { viewModel.videoPokerSetBet(vp.betAmount + 100L) },
                )
            }
        }
    }
}

@Composable
private fun PokerHandRow(hand: List<PlayingCard>, held: List<Boolean>, onToggle: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        hand.forEachIndexed { i, card ->
            val isHeld = held.getOrElse(i) { false }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                if (isHeld) Text("HOLD", fontSize = 9.sp, color = GoldPrimary, fontWeight = FontWeight.Bold)
                else Spacer(Modifier.height(14.dp))
                Surface(
                    shape    = RoundedCornerShape(6.dp),
                    color    = if (isHeld) GoldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .border(
                            width = if (isHeld) 2.dp else 1.dp,
                            color = if (isHeld) GoldPrimary else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(6.dp),
                        )
                        .clickable { onToggle(i) },
                ) {
                    val isRed = card.suit == 1 || card.suit == 2
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(card.display, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isRed) Color.Red else MaterialTheme.colorScheme.onSurface)
                            Text(card.suitSymbol, fontSize = 14.sp, color = if (isRed) Color.Red else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PokerPayTable(bet: Long) {
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Pay Table", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            PokerHand.values().forEach { hand ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(hand.label, style = MaterialTheme.typography.bodySmall)
                    Text("${(bet * hand.multiplier).formatCoins()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = GoldPrimary)
                }
            }
        }
    }
}

// ── Shared ───────────────────────────────────────────────────────────────────

@Composable
private fun BetRow(
    label: String,
    bet: Long,
    minBet: Long,
    step: Long,
    coins: Long,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        modifier             = Modifier.fillMaxWidth(),
        verticalAlignment    = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        OutlinedButton(onClick = onDecrease, enabled = bet > minBet, modifier = Modifier.size(40.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            Text("-", fontSize = 18.sp)
        }
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        OutlinedButton(onClick = onIncrease, enabled = bet + step <= coins, modifier = Modifier.size(40.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            Text("+", fontSize = 18.sp)
        }
    }
}
