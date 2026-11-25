package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.Client
import com.example.domain.model.PaymentMethod
import com.example.domain.model.PlanTemplate
import com.example.domain.model.Subscription
import com.example.i18n.AppStrings
import com.example.i18n.formatDA
import com.example.i18n.paymentMethodLabel
import com.example.ui.theme.SuiviTheme
import com.example.util.DateUtils

@Composable
fun AddEditClientDialog(
  existingClient: Client?,
  templates: List<PlanTemplate>,
  strings: AppStrings,
  onSaveNew: (
    fullName: String,
    phone: String,
    email: String,
    goal: String,
    notes: String,
    includeSub: Boolean,
    planName: String,
    priceDa: Int,
    durationDays: Int,
    startDate: String,
    sessionsTotal: Int?,
    amountPaidDa: Int,
    method: PaymentMethod,
  ) -> Unit,
  onSaveEdit: (Client) -> Unit,
  onDismiss: () -> Unit,
) {
  val colors = SuiviTheme.colors
  val isEdit = existingClient != null

  var fullName by remember { mutableStateOf(existingClient?.fullName ?: "") }
  var phone by remember { mutableStateOf(existingClient?.phone ?: "") }
  var email by remember { mutableStateOf(existingClient?.email ?: "") }
  var goal by remember { mutableStateOf(existingClient?.goal ?: "") }
  var notes by remember { mutableStateOf(existingClient?.notes ?: "") }

  var includeSub by remember { mutableStateOf(!isEdit) }
  val firstTpl = templates.firstOrNull()
  var planName by remember { mutableStateOf(firstTpl?.name ?: "Coaching 1-on-1 (1 Mois)") }
  var priceStr by remember { mutableStateOf((firstTpl?.priceDa ?: 5000).toString()) }
  var durationStr by remember { mutableStateOf((firstTpl?.durationDays ?: 30).toString()) }
  var startDate by remember { mutableStateOf(DateUtils.todayIso()) }
  var sessionsStr by remember { mutableStateOf(firstTpl?.sessionsTotal?.toString() ?: "") }
  var paidStr by remember { mutableStateOf((firstTpl?.priceDa ?: 5000).toString()) }
  var method by remember { mutableStateOf(PaymentMethod.CASH) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier = Modifier.fillMaxWidth(0.94f).heightIn(max = 680.dp),
      shape = RoundedCornerShape(24.dp),
      color = colors.cardSurface,
    ) {
      Column(
        modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = if (isEdit) strings.editClient else strings.addClient,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Filled.Close, contentDescription = strings.close, tint = colors.textMuted)
          }
        }

        OutlinedTextField(
          value = fullName,
          onValueChange = { fullName = it },
          label = { Text(strings.fullNameLabel) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("client_name_input"),
        )
        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text(strings.clientPhoneLabel) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("client_phone_input"),
        )
        OutlinedTextField(
          value = goal,
          onValueChange = { goal = it },
          label = { Text(strings.clientGoalLabel) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("client_goal_input"),
        )
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text(strings.clientNotesLabel) },
          maxLines = 3,
          modifier = Modifier.fillMaxWidth(),
        )

        if (!isEdit) {
          Row(
            modifier =
              Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.softSurface)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = strings.includeSubscriptionToggle,
              style = MaterialTheme.typography.titleSmall,
              color = colors.textPrimary,
            )
            Switch(checked = includeSub, onCheckedChange = { includeSub = it })
          }

          if (includeSub) {
            if (templates.isNotEmpty()) {
              Text(
                text = strings.selectTemplateHint,
                style = MaterialTheme.typography.labelLarge,
                color = colors.textMuted,
              )
              Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                templates.forEach { tpl ->
                  FilterChip(
                    selected = planName == tpl.name,
                    onClick = {
                      planName = tpl.name
                      priceStr = tpl.priceDa.toString()
                      paidStr = tpl.priceDa.toString()
                      durationStr = tpl.durationDays.toString()
                      sessionsStr = tpl.sessionsTotal?.toString() ?: ""
                    },
                    label = { Text("${tpl.name} • ${formatDA(tpl.priceDa)}") },
                    colors =
                      FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.accentBlue,
                        selectedLabelColor = Color.White,
                      ),
                  )
                }
              }
            }

            OutlinedTextField(
              value = planName,
              onValueChange = { planName = it },
              label = { Text(strings.planNameLabel) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              OutlinedTextField(
                value = priceStr,
                onValueChange = { priceStr = it.filter { ch -> ch.isDigit() } },
                label = { Text(strings.priceLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
              OutlinedTextField(
                value = durationStr,
                onValueChange = { durationStr = it.filter { ch -> ch.isDigit() } },
                label = { Text(strings.durationDaysLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text(strings.startDateLabel) },
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
              OutlinedTextField(
                value = sessionsStr,
                onValueChange = { sessionsStr = it.filter { ch -> ch.isDigit() } },
                label = { Text(strings.sessionsTotalLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
            }
            OutlinedTextField(
              value = paidStr,
              onValueChange = { paidStr = it.filter { ch -> ch.isDigit() } },
              label = { Text(strings.initialPaidLabel) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )
            PaymentMethodSelector(selected = method, strings = strings, onSelect = { method = it })
          }
        }

        Spacer(Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(14.dp),
          ) {
            Text(strings.cancel)
          }
          Button(
            onClick = {
              if (isEdit) {
                onSaveEdit(
                  existingClient!!.copy(
                    fullName = fullName.trim(),
                    phone = phone.trim(),
                    email = email.trim(),
                    goal = goal.trim(),
                    notes = notes.trim(),
                  )
                )
              } else {
                val validStart =
                  if (DateUtils.isValidIsoDate(startDate)) startDate else DateUtils.todayIso()
                onSaveNew(
                  fullName,
                  phone,
                  email,
                  goal,
                  notes,
                  includeSub,
                  planName,
                  priceStr.toIntOrNull() ?: 0,
                  (durationStr.toIntOrNull() ?: 30).coerceAtLeast(1),
                  validStart,
                  sessionsStr.toIntOrNull(),
                  paidStr.toIntOrNull() ?: 0,
                  method,
                )
              }
            },
            enabled = fullName.isNotBlank(),
            modifier = Modifier.weight(1.3f).height(48.dp).testTag("save_client_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.accentBlue),
          ) {
            Text(strings.save, color = Color.White)
          }
        }
      }
    }
  }
}

@Composable
fun RenewSubscriptionDialog(
  clientName: String,
  latestSubscription: Subscription?,
  templates: List<PlanTemplate>,
  strings: AppStrings,
  onConfirm: (
    planName: String,
    priceDa: Int,
    durationDays: Int,
    startDate: String,
    sessionsTotal: Int?,
    amountPaidDa: Int,
    method: PaymentMethod,
    note: String,
  ) -> Unit,
  onDismiss: () -> Unit,
) {
  val colors = SuiviTheme.colors
  val firstTpl = templates.firstOrNull()
  var planName by remember { mutableStateOf(latestSubscription?.planName ?: firstTpl?.name ?: "Coaching 1-on-1") }
  var priceStr by remember { mutableStateOf((latestSubscription?.priceDa ?: firstTpl?.priceDa ?: 5000).toString()) }
  var durationStr by remember { mutableStateOf((latestSubscription?.durationDays ?: firstTpl?.durationDays ?: 30).toString()) }
  var sessionsStr by remember { mutableStateOf((latestSubscription?.sessionsTotal ?: firstTpl?.sessionsTotal)?.toString() ?: "") }
  var paidStr by remember { mutableStateOf(priceStr) }
  var method by remember { mutableStateOf(PaymentMethod.CASH) }
  var note by remember { mutableStateOf("") }

  val canChainFromEnd =
    latestSubscription != null && latestSubscription.daysRemaining > 0
  var startFromEnd by remember { mutableStateOf(canChainFromEnd) }
  val resolvedStart =
    if (startFromEnd && latestSubscription != null) latestSubscription.endDate
    else DateUtils.todayIso()

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier = Modifier.fillMaxWidth(0.94f).heightIn(max = 660.dp),
      shape = RoundedCornerShape(24.dp),
      color = colors.cardSurface,
    ) {
      Column(
        modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = strings.renewAction,
              style = MaterialTheme.typography.titleLarge,
              color = colors.textPrimary,
            )
            Text(
              text = clientName,
              style = MaterialTheme.typography.bodyMedium,
              color = colors.accentCyan,
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Filled.Close, contentDescription = strings.close, tint = colors.textMuted)
          }
        }

        if (templates.isNotEmpty()) {
          Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            templates.forEach { tpl ->
              FilterChip(
                selected = planName == tpl.name,
                onClick = {
                  planName = tpl.name
                  priceStr = tpl.priceDa.toString()
                  paidStr = tpl.priceDa.toString()
                  durationStr = tpl.durationDays.toString()
                  sessionsStr = tpl.sessionsTotal?.toString() ?: ""
                },
                label = { Text("${tpl.name} • ${formatDA(tpl.priceDa)}") },
                colors =
                  FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.accentBlue,
                    selectedLabelColor = Color.White,
                  ),
              )
            }
          }
        }

        if (canChainFromEnd) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            FilterChip(
              selected = !startFromEnd,
              onClick = { startFromEnd = false },
              label = { Text(strings.renewFromToday) },
              modifier = Modifier.weight(1f),
            )
            FilterChip(
              selected = startFromEnd,
              onClick = { startFromEnd = true },
              label = { Text(strings.renewFromEndDate) },
              modifier = Modifier.weight(1f),
            )
          }
        }

        OutlinedTextField(
          value = planName,
          onValueChange = { planName = it },
          label = { Text(strings.planNameLabel) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedTextField(
            value = priceStr,
            onValueChange = { priceStr = it.filter { ch -> ch.isDigit() } },
            label = { Text(strings.priceLabel) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = durationStr,
            onValueChange = { durationStr = it.filter { ch -> ch.isDigit() } },
            label = { Text(strings.durationDaysLabel) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedTextField(
            value = paidStr,
            onValueChange = { paidStr = it.filter { ch -> ch.isDigit() } },
            label = { Text(strings.initialPaidLabel) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = sessionsStr,
            onValueChange = { sessionsStr = it.filter { ch -> ch.isDigit() } },
            label = { Text(strings.sessionsTotalLabel) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
        }

        PaymentMethodSelector(selected = method, strings = strings, onSelect = { method = it })

        OutlinedTextField(
          value = note,
          onValueChange = { note = it },
          label = { Text(strings.paymentNoteLabel) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(14.dp),
          ) {
            Text(strings.cancel)
          }
          Button(
            onClick = {
              onConfirm(
                planName,
                priceStr.toIntOrNull() ?: 0,
                (durationStr.toIntOrNull() ?: 30).coerceAtLeast(1),
                resolvedStart,
                sessionsStr.toIntOrNull(),
                paidStr.toIntOrNull() ?: 0,
                method,
                note,
              )
            },
            enabled = planName.isNotBlank(),
            modifier = Modifier.weight(1.3f).height(48.dp).testTag("confirm_renew_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.accentBlue),
          ) {
            Text(strings.confirm, color = Color.White)
          }
        }
      }
    }
  }
}

@Composable
fun AddPaymentDialog(
  clientName: String,
  suggestedAmountDa: Int,
  strings: AppStrings,
  onConfirm: (amountDa: Int, method: PaymentMethod, note: String) -> Unit,
  onDismiss: () -> Unit,
) {
  val colors = SuiviTheme.colors
  var amountStr by remember {
    mutableStateOf(if (suggestedAmountDa > 0) suggestedAmountDa.toString() else "")
  }
  var method by remember { mutableStateOf(PaymentMethod.CASH) }
  var note by remember { mutableStateOf("") }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = colors.cardSurface,
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = "${strings.addPaymentAction} • $clientName",
          style = MaterialTheme.typography.titleLarge,
          color = colors.textPrimary,
        )
        OutlinedTextField(
          value = amountStr,
          onValueChange = { amountStr = it.filter { ch -> ch.isDigit() } },
          label = { Text(strings.initialPaidLabel) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("payment_amount_input"),
        )
        PaymentMethodSelector(selected = method, strings = strings, onSelect = { method = it })
        OutlinedTextField(
          value = note,
          onValueChange = { note = it },
          label = { Text(strings.paymentNoteLabel) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(14.dp),
          ) {
            Text(strings.cancel)
          }
          Button(
            onClick = {
              val amt = amountStr.toIntOrNull() ?: 0
              if (amt > 0) onConfirm(amt, method, note)
            },
            enabled = (amountStr.toIntOrNull() ?: 0) > 0,
            modifier = Modifier.weight(1.3f).height(48.dp).testTag("confirm_payment_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.statusActive),
          ) {
            Text(strings.save, color = Color.White)
          }
        }
      }
    }
  }
}

@Composable
fun AddPlanTemplateDialog(
  strings: AppStrings,
  onAdd: (name: String, durationDays: Int, priceDa: Int, sessionsTotal: Int?) -> Unit,
  onDismiss: () -> Unit,
) {
  val colors = SuiviTheme.colors
  var name by remember { mutableStateOf("") }
  var priceStr by remember { mutableStateOf("5000") }
  var durationStr by remember { mutableStateOf("30") }
  var sessionsStr by remember { mutableStateOf("") }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = colors.cardSurface,
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = strings.addPlanTemplate,
          style = MaterialTheme.typography.titleLarge,
          color = colors.textPrimary,
        )
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(strings.planNameLabel) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = priceStr,
            onValueChange = { priceStr = it.filter { ch -> ch.isDigit() } },
            label = { Text(strings.priceLabel) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = durationStr,
            onValueChange = { durationStr = it.filter { ch -> ch.isDigit() } },
            label = { Text(strings.durationDaysLabel) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
        }
        OutlinedTextField(
          value = sessionsStr,
          onValueChange = { sessionsStr = it.filter { ch -> ch.isDigit() } },
          label = { Text(strings.sessionsTotalLabel) },
          placeholder = { Text(strings.sessionsOptionalHint) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(14.dp),
          ) {
            Text(strings.cancel)
          }
          Button(
            onClick = {
              onAdd(
                name,
                (durationStr.toIntOrNull() ?: 30).coerceAtLeast(1),
                priceStr.toIntOrNull() ?: 0,
                sessionsStr.toIntOrNull(),
              )
            },
            enabled = name.isNotBlank(),
            modifier = Modifier.weight(1.3f).height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.accentBlue),
          ) {
            Text(strings.save, color = Color.White)
          }
        }
      }
    }
  }
}

@Composable
private fun PaymentMethodSelector(
  selected: PaymentMethod,
  strings: AppStrings,
  onSelect: (PaymentMethod) -> Unit,
) {
  val colors = SuiviTheme.colors
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text(
      text = strings.paymentMethodLabel,
      style = MaterialTheme.typography.labelLarge,
      color = colors.textMuted,
    )
    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      PaymentMethod.entries.forEach { method ->
        val isSelected = method == selected
        Box(
          modifier =
            Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(if (isSelected) colors.accentBlue else colors.softSurface)
              .clickable { onSelect(method) }
              .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
          Text(
            text = strings.paymentMethodLabel(method),
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) Color.White else colors.textMuted,
          )
        }
      }
    }
  }
}
