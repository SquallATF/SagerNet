/******************************************************************************
 *                                                                            *
 * Copyright (C) 2021 by nekohasekai <sekai@neko.services>                    *
 * Copyright (C) 2021 by Max Lv <max.c.lv@gmail.com>                          *
 * Copyright (C) 2021 by Mygod Studio <contact-shadowsocks-android@mygod.be>  *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 *  (at your option) any later version.                                       *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program. If not, see <http://www.gnu.org/licenses/>.       *
 *                                                                            *
 ******************************************************************************/

package io.nekohasekai.sagernet.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.takisoft.preferencex.PreferenceFragmentCompat
import io.nekohasekai.sagernet.Key
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.SagerNet
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.preference.EditTextPreferenceModifiers
import io.nekohasekai.sagernet.ktx.*
import io.nekohasekai.sagernet.utils.Theme
import io.nekohasekai.sagernet.widget.ColorPickerPreference

class SettingsPreferenceFragment : PreferenceFragmentCompat() {

    private lateinit var isProxyApps: SwitchPreference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addOverScrollListener(listView)
    }

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = DataStore.configurationStore
        DataStore.initGlobal()
        addPreferencesFromResource(R.xml.global_preferences)
        val appTheme = findPreference<ColorPickerPreference>(Key.APP_THEME)!!
        if (!isExpert) {
            appTheme.remove()
        } else {
            appTheme.setOnPreferenceChangeListener { _, newTheme ->
                if (serviceStarted()) {
                    SagerNet.reloadService()
                }
                val theme = Theme.getTheme(newTheme as Int)
                app.setTheme(theme)
                requireActivity().apply {
                    setTheme(theme)
                    recreate()
                }
                true
            }
        }

        val portSocks5 = findPreference<EditTextPreference>(Key.SOCKS_PORT)!!
        val speedInterval = findPreference<Preference>(Key.SPEED_INTERVAL)!!
        val serviceMode = findPreference<Preference>(Key.SERVICE_MODE)!!
        val allowAccess = findPreference<Preference>(Key.ALLOW_ACCESS)!!
        val requireHttp = findPreference<SwitchPreference>(Key.REQUIRE_HTTP)!!
        val portHttp = findPreference<EditTextPreference>(Key.HTTP_PORT)!!
        val showStopButton = findPreference<SwitchPreference>(Key.SHOW_STOP_BUTTON)!!
        if (Build.VERSION.SDK_INT < 24) {
            showStopButton.remove()
        }
        val showDirectSpeed = findPreference<SwitchPreference>(Key.SHOW_DIRECT_SPEED)!!
        val ipv6Route = findPreference<Preference>(Key.IPV6_ROUTE)!!
        val preferIpv6 = findPreference<Preference>(Key.PREFER_IPV6)!!
        val domainStrategy = findPreference<Preference>(Key.DOMAIN_STRATEGY)!!
        val domainMatcher = findPreference<Preference>(Key.DOMAIN_MATCHER)!!
        if (!isExpert) {
            domainMatcher.remove()
        }

        val trafficSniffing = findPreference<Preference>(Key.TRAFFIC_SNIFFING)!!
        val enableMux = findPreference<Preference>(Key.ENABLE_MUX)!!
        val enableMuxForAll = findPreference<Preference>(Key.ENABLE_MUX_FOR_ALL)!!
        val muxConcurrency = findPreference<EditTextPreference>(Key.MUX_CONCURRENCY)!!
        val tcpKeepAliveInterval = findPreference<EditTextPreference>(Key.TCP_KEEP_ALIVE_INTERVAL)!!

        val bypassLan = findPreference<Preference>(Key.BYPASS_LAN)!!

        val forceShadowsocksRust =
            findPreference<SwitchPreference>(Key.FORCE_SHADOWSOCKS_RUST)!!
        if (!isExpert) {
            forceShadowsocksRust.remove()
        }

        val remoteDns = findPreference<Preference>(Key.REMOTE_DNS)!!
        val enableLocalDns = findPreference<SwitchPreference>(Key.ENABLE_LOCAL_DNS)!!
        val portLocalDns = findPreference<EditTextPreference>(Key.LOCAL_DNS_PORT)!!
        val domesticDns = findPreference<EditTextPreference>(Key.DOMESTIC_DNS)!!

        portLocalDns.setOnBindEditTextListener(EditTextPreferenceModifiers.Port)
        muxConcurrency.setOnBindEditTextListener(EditTextPreferenceModifiers.Port)
        portSocks5.setOnBindEditTextListener(EditTextPreferenceModifiers.Port)
        portHttp.setOnBindEditTextListener(EditTextPreferenceModifiers.Port)

        val metedNetwork = findPreference<Preference>(Key.METERED_NETWORK)!!
        if (Build.VERSION.SDK_INT < 28) {
            metedNetwork.remove()
        }
        isProxyApps = findPreference(Key.PROXY_APPS)!!
        isProxyApps.setOnPreferenceChangeListener { _, newValue ->
            startActivity(Intent(activity, AppManagerActivity::class.java))
            if (newValue as Boolean) DataStore.dirty = true
            newValue
        }

        val reloadListener = Preference.OnPreferenceChangeListener { _, _ ->
            needReload()
            true
        }

        serviceMode.onPreferenceChangeListener = reloadListener
        speedInterval.onPreferenceChangeListener = reloadListener
        portSocks5.onPreferenceChangeListener = reloadListener
        requireHttp.onPreferenceChangeListener = reloadListener
        portHttp.onPreferenceChangeListener = reloadListener
        showStopButton.onPreferenceChangeListener = reloadListener
        showDirectSpeed.onPreferenceChangeListener = reloadListener
        domainStrategy.onPreferenceChangeListener = reloadListener
        domainMatcher.onPreferenceChangeListener = reloadListener
        trafficSniffing.onPreferenceChangeListener = reloadListener
        enableMux.onPreferenceChangeListener = reloadListener
        enableMuxForAll.onPreferenceChangeListener = reloadListener
        muxConcurrency.onPreferenceChangeListener = reloadListener
        tcpKeepAliveInterval.onPreferenceChangeListener = reloadListener
        bypassLan.onPreferenceChangeListener = reloadListener
        forceShadowsocksRust.onPreferenceChangeListener = reloadListener
        remoteDns.onPreferenceChangeListener = reloadListener
        enableLocalDns.onPreferenceChangeListener = reloadListener
        portLocalDns.onPreferenceChangeListener = reloadListener
        domesticDns.onPreferenceChangeListener = reloadListener
        ipv6Route.onPreferenceChangeListener = reloadListener
        preferIpv6.onPreferenceChangeListener = reloadListener
        allowAccess.onPreferenceChangeListener = reloadListener

    }

    override fun onResume() {
        super.onResume()

        if (::isProxyApps.isInitialized) {
            isProxyApps.isChecked = DataStore.proxyApps
        }
    }

}