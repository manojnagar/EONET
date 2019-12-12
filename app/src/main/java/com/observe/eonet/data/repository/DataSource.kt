package com.observe.eonet.data.repository

import com.observe.eonet.data.repository.remote.CategoryApi
import com.observe.eonet.data.repository.remote.EventApi

/**
 * Contract for a database repository
 */
interface DataSource : EventApi, CategoryApi