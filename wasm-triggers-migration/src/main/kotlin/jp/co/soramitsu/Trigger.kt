package jp.co.soramitsu

import jp.co.soramitsu.iroha2.generated.AccountEventFilter
import jp.co.soramitsu.iroha2.generated.AccountId
import jp.co.soramitsu.iroha2.generated.Duration
import jp.co.soramitsu.iroha2.generated.FilterOptOfDataEntityFilter
import jp.co.soramitsu.iroha2.generated.OriginFilterOfAccountEvent
import jp.co.soramitsu.iroha2.generated.TriggeringFilterBox
import jp.co.soramitsu.iroha2.transaction.EntityFilters
import jp.co.soramitsu.iroha2.transaction.EventFilters
import jp.co.soramitsu.iroha2.transaction.Filters
import java.math.BigInteger
import java.util.Date

enum class RepeatsEnum(val num: Int) {
    INDEFINITELY(0),
    EXACTLY(1);

    companion object {
        fun from(num: Int): RepeatsEnum {
            return when (num) {
                INDEFINITELY.num -> INDEFINITELY
                EXACTLY.num -> EXACTLY
                else -> throw RuntimeException("Repeats $num not supported")
            }
        }
    }
}

enum class TriggerType(val num: Int) {
    TIME(0),
    DATA_BY_ACCOUNT_METADATA(1);

    companion object {
        fun from(num: Int): TriggerType {
            return when (num) {
                TIME.num -> TIME
                DATA_BY_ACCOUNT_METADATA.num -> DATA_BY_ACCOUNT_METADATA
                else -> throw RuntimeException("Trigger type $num not supported")
            }
        }
    }
}

fun getTimeTrigger(timeInterval: Long): TriggeringFilterBox.Time {
    val currentTime = Date().time / 1000
    return TriggeringFilterBox.Time(
        EventFilters.timeEventFilter(
            Duration(BigInteger.valueOf(currentTime), 0),
            Duration(BigInteger.valueOf(timeInterval), 0)
        )
    )
}

fun getDataTriggerByAccountMetadataInserted(accountId: AccountId? = null): TriggeringFilterBox.Data {
    var accountEvent: OriginFilterOfAccountEvent? = null
    if (accountId != null) {
        accountEvent = OriginFilterOfAccountEvent(accountId)
    }
    return TriggeringFilterBox.Data(
        FilterOptOfDataEntityFilter.BySome(
            EntityFilters.byAccount(
                idFilter = accountEvent,
                eventFilter = AccountEventFilter.ByMetadataInserted()
            )
        )
    )
}
