package org.secfirst.umbrella.whitelabel.feature.reader.interactor

import kotlinx.coroutines.experimental.Deferred
import okhttp3.ResponseBody
import org.secfirst.umbrella.whitelabel.data.database.content.ContentRepo
import org.secfirst.umbrella.whitelabel.data.database.reader.FeedLocation
import org.secfirst.umbrella.whitelabel.data.database.reader.FeedSource
import org.secfirst.umbrella.whitelabel.data.database.reader.RSS
import org.secfirst.umbrella.whitelabel.data.database.reader.ReaderRepo
import org.secfirst.umbrella.whitelabel.data.network.ApiHelper
import org.secfirst.umbrella.whitelabel.data.preferences.AppPreferenceHelper
import org.secfirst.umbrella.whitelabel.feature.base.interactor.BaseInteractorImp
import javax.inject.Inject

class ReaderInteractorImp @Inject constructor(apiHelper: ApiHelper,
                                              preferenceHelper: AppPreferenceHelper,
                                              contentRepo: ContentRepo,
                                              private val readerRepo: ReaderRepo)

    : BaseInteractorImp(apiHelper, preferenceHelper, contentRepo), ReaderBaseInteractor {

    override suspend fun applyChangeDatabaseAccess(userToken: String): Boolean {
        val res = readerRepo.changeToken(userToken)
        if (res) {
            preferenceHelper.setLoggedIn(true)
            preferenceHelper.setSkipPassword(true)
        }
        return res
    }

    override fun setSkipPassword(value: Boolean) = preferenceHelper.setSkipPassword(value)

    override fun isSkipPassword() = preferenceHelper.getSkipPassword()

    override suspend fun deleteLocation() = readerRepo.deleteLocation()

    override suspend fun fetchRefreshInterval() = preferenceHelper.getRefreshInterval()

    override suspend fun putRefreshInterval(position: Int) = preferenceHelper.setRefreshInterval(position)

    override suspend fun insertFeedLocation(feedLocation: FeedLocation) = readerRepo.saveFeedLocation(feedLocation)

    override suspend fun insertAllFeedSources(feedSources: List<FeedSource>) = readerRepo.saveAllFeedSources(feedSources)

    override suspend fun fetchFeedSources() = readerRepo.getAllFeedSources()

    override suspend fun fetchFeedLocation() = readerRepo.getFeedLocation()

    override suspend fun insertAllRss(rssList: List<RSS>) = readerRepo.saveAllRss(rssList)

    override suspend fun doRSsCall(url: String): Deferred<ResponseBody> = apiHelper.getRss(url)

    override suspend fun doFeedCall(countryCode: String,
                                    source: String,
                                    since: String) = apiHelper.getFeedList(countryCode, source, since)

    override suspend fun deleteRss(rss: RSS) = readerRepo.delete(rss)

    override suspend fun insertRss(rss: RSS) = readerRepo.saveRss(rss)

    override suspend fun fetchRss(): List<RSS> = readerRepo.getAllRss()
}