package es.ulpgc.kippo.model

import com.google.firebase.firestore.DocumentId

data class Reward(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val cost: Long = 0,
    val icon: String = "redeem",
    val householdId: String = ""
) {
    companion object {
        val SAMPLE_REWARDS = listOf(
            Reward("1", "Pizza Night", "A delicious pizza for the whole household", 500, "pizza"),
            Reward("2", "Movie Night", "Rent any movie and some popcorn", 300, "movie"),
            Reward("3", "Game Pass", "One week of gaming without chores", 1000, "game"),
            Reward("4", "House Star", "Choose the next household activity", 200, "star")
        )
    }
}
