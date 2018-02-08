/*
 * Copyright (c) 2012 Google Inc.
 
 * Hak Cipta (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 
 * Berlisensi di bawah Lisensi Apache, Versi 2.0 ("Lisensi");
 * Anda mungkin tidak menggunakan file ini kecuali sesuai dengan Lisensi.
 * Anda bisa mendapatkan salinan Lisensi di
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 
 * Kecuali diwajibkan oleh hukum yang berlaku atau disepakati secara tertulis, perangkat lunak
 * didistribusikan di bawah Lisensi didistribusikan pada "SEBAGAIMANA ADANYA" BASIS,
 * TANPA JAMINAN ATAU KETENTUAN DALAM BENTUK APAPUN, baik tersurat maupun tersirat.
 * Lihat Lisensi untuk bahasa tertentu yang mengatur hak akses dan
 * Keterbatasan di bawah Lisensi.
 
 */

package com.android.vending.billing;

import android.os.Bundle;

paket com.android.vending.billing;

impor android.os.Bundle;

/**
 * InAppBillingService is the service that provides in-app billing version 3 and beyond.
 * This service provides the following features:
 * 1. Provides a new API to get details of in-app items published for the app including
 *    price, type, title and description.
 * 2. The purchase flow is synchronous and purchase information is available immediately
 *    after it completes.
 * 3. Purchase information of in-app purchases is maintained within the Google Play system
 *    till the purchase is consumed.
 * 4. An API to consume a purchase of an inapp item. All purchases of one-time
 *    in-app items are consumable and thereafter can be purchased again.
 * 5. An API to get current purchases of the user immediately. This will not contain any
 *    consumed purchases.
 
 * InAppBillingService adalah layanan yang menyediakan penagihan dalam aplikasi versi 3 dan yang lebih tinggi.
 * Layanan ini menyediakan beberapa fitur berikut:
 * 1. Menyediakan API baru untuk mendapatkan detail item dalam aplikasi yang dipublikasikan untuk aplikasi termasuk
 * harga, jenis, judul dan deskripsi.
 * 2. Aliran pembelian sinkron dan informasi pembelian segera tersedia
 * setelah selesai
 * 3. Membeli informasi pembelian dalam aplikasi dipertahankan dalam sistem Google Play
 * sampai pembelian habis.
 * 4. API untuk mengkonsumsi pembelian item inapp. Semua pembelian satu kali
 * Item dalam aplikasi habis dan selanjutnya bisa dibeli lagi.
 * 5. API untuk segera membeli pengguna saat ini. Ini tidak akan berisi apapun
 * pembelian yang dikonsumsi
 *
 * All calls will give a response code with the following possible values
 * RESULT_OK = 0 - success
 * RESULT_USER_CANCELED = 1 - User pressed back or canceled a dialog
 * RESULT_SERVICE_UNAVAILABLE = 2 - The network connection is down
 * RESULT_BILLING_UNAVAILABLE = 3 - This billing API version is not supported for the type requested
 * RESULT_ITEM_UNAVAILABLE = 4 - Requested SKU is not available for purchase
 * RESULT_DEVELOPER_ERROR = 5 - Invalid arguments provided to the API
 * RESULT_ERROR = 6 - Fatal error during the API action
 * RESULT_ITEM_ALREADY_OWNED = 7 - Failure to purchase since item is already owned
 * RESULT_ITEM_NOT_OWNED = 8 - Failure to consume since item is not owned
 
 * Semua panggilan akan memberikan kode tanggapan dengan nilai yang mungkin berikut
 * RESULT_OK = 0 - sukses
 * RESULT_USER_CANCELED = 1 - Pengguna menekan kembali atau membatalkan sebuah dialog
 * RESULT_SERVICE_UNAVAILABLE = 2 - Sambungan jaringan sedang down
 * RESULT_BILLING_UNAVAILABLE = 3 - Versi API penagihan ini tidak didukung untuk jenis yang diminta
 * RESULT_ITEM_UNAVAILABLE = 4 - SKU yang diminta tidak tersedia untuk pembelian
 * RESULT_DEVELOPER_ERROR = 5 - Argumen tidak valid yang diberikan ke API
 * RESULT_ERROR = 6 - Kesalahan fatal selama tindakan API
 * RESULT_ITEM_ALREADY_OWNED = 7 - Gagal membeli karena barang sudah dimiliki
 * RESULT_ITEM_NOT_OWNED = 8 - Gagal mengkonsumsi karena item tidak dimiliki
 */
interface IInAppBillingService {

antarmuka IInAppBillingService {

    /**
     * Checks support for the requested billing API version, package and in-app type.
     * Minimum API version supported by this interface is 3.
     * @param apiVersion billing API version that the app is using
     * @param packageName the package name of the calling app
     * @param type type of the in-app item being purchased ("inapp" for one-time purchases
     *        and "subs" for subscriptions)
     * @return RESULT_OK(0) on success and appropriate response code on failures.
     */
    int isBillingSupported(int apiVersion, String packageName, String type);
    
     * Memeriksa dukungan untuk versi API penagihan yang diminta, paket dan jenis dalam aplikasi.
     * Versi API Minimum yang didukung oleh antarmuka ini adalah 3.
     * @param apiVersion billing API versi yang aplikasi ini gunakan
     * @param packageName nama paket aplikasi pemanggil
     * @param jenis jenis item dalam aplikasi yang dibeli ("inapp" untuk pembelian satu kali
     * dan "subs" untuk langganan)
     * @return RESULT_OK (0) tentang kesuksesan dan kode tanggapan yang sesuai pada kegagalan.
     * /
     int isBillingSupported (int apiVersion, String packageName, tipe String);

    /**
     * Provides details of a list of SKUs
     * Given a list of SKUs of a valid type in the skusBundle, this returns a bundle
     * with a list JSON strings containing the productId, price, title and description.
     * This API can be called with a maximum of 20 SKUs.
     * @param apiVersion billing API version that the app is using
     * @param packageName the package name of the calling app
     * @param type of the in-app items ("inapp" for one-time purchases
     *        and "subs" for subscriptions)
     * @param skusBundle bundle containing a StringArrayList of SKUs with key "ITEM_ID_LIST"
     * @return Bundle containing the following key-value pairs
     *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, appropriate response codes
     *                         on failures.
     *         "DETAILS_LIST" with a StringArrayList containing purchase information
     *                        in JSON format similar to:
     *                        '{ "productId" : "exampleSku",
     *                           "type" : "inapp",
     *                           "price" : "$5.00",
     *                           "price_currency": "USD",
     *                           "price_amount_micros": 5000000,
     *                           "title : "Example Title",
     *                           "description" : "This is an example description" }'
     
     * Menyediakan rincian daftar SKU
     * Dengan daftar SKU tipe yang valid di skusBundle, ini mengembalikan sebuah paket
     * dengan daftar string JSON yang berisi productId, harga, judul dan deskripsi.
     * API ini bisa disebut dengan maksimal 20 SKU.
     * @param apiVersion billing API versi yang aplikasi ini gunakan
     * @param packageName nama paket aplikasi pemanggil
     * @param jenis item dalam aplikasi ("inapp" untuk pembelian satu kali
     * dan "subs" untuk langganan)
     * @param skusBundle bundle berisi StringArrayList SKU dengan kunci "ITEM_ID_LIST"
     * @return Bundle yang berisi pasangan kunci-nilai berikut
     * "RESPONSE_CODE" dengan nilai int, RESULT_OK (0) jika berhasil, kode respon yang sesuai
     * pada kegagalan
     * "DETAILS_LIST" dengan StringArrayList berisi informasi pembelian
     * dalam format JSON mirip dengan:
     * '{"productId": "exampleSku",
     * "type": "inapp",
     * "harga": "$ 5,00",
     * "price_currency": "USD",
     * "price_amount_micros": 5000000,
     * "title:" Contoh Judul ",
     * "deskripsi": "Ini adalah contoh deskripsi"} '
     
     */
    Bundle getSkuDetails(int apiVersion, String packageName, String type, in Bundle skusBundle);

    /**
     * Returns a pending intent to launch the purchase flow for an in-app item by providing a SKU,
     * the type, a unique purchase token and an optional developer payload.
     * @param apiVersion billing API version that the app is using
     * @param packageName package name of the calling app
     * @param sku the SKU of the in-app item as published in the developer console
     * @param type of the in-app item being purchased ("inapp" for one-time purchases
     *        and "subs" for subscriptions)
     * @param developerPayload optional argument to be sent back with the purchase information
     * @return Bundle containing the following key-value pairs
     *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, appropriate response codes
     *                         on failures.
     *         "BUY_INTENT" - PendingIntent to start the purchase flow
     
     Bundle getSkuDetails (int apiVersion, String packageName, String type, di Bundle skusBundle);

     / **
      * Mengembalikan tujuan yang tertunda untuk meluncurkan arus pembelian untuk item dalam aplikasi dengan menyediakan SKU,
      * Tipe, token pembelian unik dan muatan pengembang opsional.
      * @param apiVersion billing API versi yang aplikasi ini gunakan
      * @param packageName nama paket dari aplikasi pemanggil
      * @param sku SKU item dalam aplikasi yang dipublikasikan di konsol pengembang
      * @param jenis item dalam aplikasi yang dibeli ("inapp" untuk pembelian satu kali
      * dan "subs" untuk langganan)
      * @param developerPayload argumen opsional untuk dikirim kembali dengan informasi pembelian
      * @return Bundle yang berisi pasangan kunci-nilai berikut
      * "RESPONSE_CODE" dengan nilai int, RESULT_OK (0) jika berhasil, kode respon yang sesuai
      * pada kegagalan
      * "BUY_INTENT" - PendingIntent untuk memulai arus pembelian
      
     *
     * The Pending intent should be launched with startIntentSenderForResult. When purchase flow
     * has completed, the onActivityResult() will give a resultCode of OK or CANCELED.
     * If the purchase is successful, the result data will contain the following key-value pairs
     *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, appropriate response
     *                         codes on failures.
     *         "INAPP_PURCHASE_DATA" - String in JSON format similar to
     *                                 '{"orderId":"12999763169054705758.1371079406387615",
     *                                   "packageName":"com.example.app",
     *                                   "productId":"exampleSku",
     *                                   "purchaseTime":1345678900000,
     *                                   "purchaseToken" : "122333444455555",
     *                                   "developerPayload":"example developer payload" }'
     *         "INAPP_DATA_SIGNATURE" - String containing the signature of the purchase data that
     *                                  was signed with the private key of the developer
     *                                  TODO: change this to app-specific keys.
     */
    Bundle getBuyIntent(int apiVersion, String packageName, String sku, String type,
        String developerPayload);
        
        * Tujuan Pending harus diluncurkan dengan startIntentSenderForResult. Saat arus beli
     * telah selesai, onActivityResult () akan memberikan resultCode OK atau DIBATALKAN.
     * Jika pembelian berhasil, data hasil akan berisi pasangan kunci-nilai berikut
     * "RESPONSE_CODE" dengan nilai int, RESULT_OK (0) jika sukses, respon yang tepat
     * kode pada kegagalan
     * "INAPP_PURCHASE_DATA" - String dalam format JSON mirip dengan
     * '{"orderId": "12999763169054705758.1371079406387615",
     * "packageName": "com.example.app",
     * "productId": "exampleSku",
     * "purchaseTime": 1345678900000,
     * "purchaseToken": "122333444455555",
     * "developerPayload": "contoh payload pengembang"} '
     * "INAPP_DATA_SIGNATURE" - String berisi tanda tangan dari data pembelian itu
     * ditandatangani dengan kunci privat pengembang
     * TODO: ubah ini ke kunci khusus aplikasi.
     * /
    Bundle getBuyIntent (int apiVersion, String packageName, String sku, tipe String,
        Pengembang StringPayload);

    /**
     * Returns the current SKUs owned by the user of the type and package name specified along with
     * purchase information and a signature of the data to be validated.
     * This will return all SKUs that have been purchased in V3 and managed items purchased using
     * V1 and V2 that have not been consumed.
     * @param apiVersion billing API version that the app is using
     * @param packageName package name of the calling app
     * @param type of the in-app items being requested ("inapp" for one-time purchases
     *        and "subs" for subscriptions)
     * @param continuationToken to be set as null for the first call, if the number of owned
     *        skus are too many, a continuationToken is returned in the response bundle.
     *        This method can be called again with the continuation token to get the next set of
     *        owned skus.
     * @return Bundle containing the following key-value pairs
     *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, appropriate response codes
                               on failures.
     *         "INAPP_PURCHASE_ITEM_LIST" - StringArrayList containing the list of SKUs
     *         "INAPP_PURCHASE_DATA_LIST" - StringArrayList containing the purchase information
     *         "INAPP_DATA_SIGNATURE_LIST"- StringArrayList containing the signatures
     *                                      of the purchase information
     *         "INAPP_CONTINUATION_TOKEN" - String containing a continuation token for the
     *                                      next set of in-app purchases. Only set if the
     *                                      user has more owned skus than the current list.
     */
    Bundle getPurchases(int apiVersion, String packageName, String type, String continuationToken);
    
    * Mengembalikan SKU saat ini yang dimiliki oleh pengguna dari jenis dan nama paket yang ditentukan bersama
     * Membeli informasi dan tanda tangan dari data yang akan divalidasi.
     * Ini akan mengembalikan semua SKU yang telah dibeli di V3 dan barang yang dikelola dibeli dengan menggunakan
     * V1 dan V2 yang belum dikonsumsi.
     * @param apiVersion billing API versi yang aplikasi ini gunakan
     * @param packageName nama paket dari aplikasi pemanggil
     * @param jenis barang dalam aplikasi yang diminta ("inapp" untuk pembelian satu kali
     * dan "subs" untuk langganan)
     * @param continuationToken untuk ditetapkan sebagai null untuk panggilan pertama, jika jumlah dimiliki
     * Skus terlalu banyak, sebuah kelanjutanToken dikembalikan ke dalam berkas respon.
     * Metode ini bisa dipanggil lagi dengan token lanjutan untuk mendapatkan set berikutnya
     * dimiliki skus
     * @return Bundle yang berisi pasangan kunci-nilai berikut
     * "RESPONSE_CODE" dengan nilai int, RESULT_OK (0) jika berhasil, kode respon yang sesuai
                               pada kegagalan
     * "INAPP_PURCHASE_ITEM_LIST" - StringArrayList berisi daftar SKU
     * "INAPP_PURCHASE_DATA_LIST" - StringArrayList berisi informasi pembelian
     * "INAPP_DATA_SIGNATURE_LIST" - StringArrayList yang berisi tanda tangan
     * dari informasi pembelian
     * "INAPP_CONTINUATION_TOKEN" - String berisi token lanjutan untuk
     * Kumpulan pembelian dalam aplikasi berikutnya. Atur saja jika
     Pengguna memiliki lebih banyak fitur skus daripada daftar saat ini.
     * /
    Bundle getPurchases (int apiVersion, String packageName, String type, String continuationToken);

    /**
     * Consume the last purchase of the given SKU. This will result in this item being removed
     * from all subsequent responses to getPurchases() and allow re-purchase of this item.
     * @param apiVersion billing API version that the app is using
     * @param packageName package name of the calling app
     * @param purchaseToken token in the purchase information JSON that identifies the purchase
     *        to be consumed
     * @return RESULT_OK(0) if consumption succeeded, appropriate response codes on failures.
     */
    int consumePurchase(int apiVersion, String packageName, String purchaseToken);

    /**
     * This API is currently under development.
     */
    int stub(int apiVersion, String packageName, String type);
    
    * Mengkonsumsi pembelian terakhir dari SKU yang diberikan. Ini akan mengakibatkan item ini dihapus
      * dari semua tanggapan selanjutnya terhadap getPurchases () dan memperbolehkan pembelian kembali item ini.
      * @param apiVersion billing API versi yang aplikasi ini gunakan
      * @param packageName nama paket dari aplikasi pemanggil
      * @param purchaseToken token dalam informasi pembelian JSON yang mengidentifikasi pembelian
      * untuk dikonsumsi
      * @return RESULT_OK (0) jika konsumsi berhasil, kode respons yang sesuai pada kegagalan.
      * /
     int consumPurchase (int apiVersion, String packageName, String purchaseToken);

     / **
      * API ini saat ini dalam pengembangan.
      * /
     int stub (int apiVersion, String packageName, tipe String);

    /**
     * Returns a pending intent to launch the purchase flow for upgrading or downgrading a
     * subscription. The existing owned SKU(s) should be provided along with the new SKU that
     * the user is upgrading or downgrading to.
     * @param apiVersion billing API version that the app is using, must be 5 or later
     * @param packageName package name of the calling app
     * @param oldSkus the SKU(s) that the user is upgrading or downgrading from,
     *        if null or empty this method will behave like {@link #getBuyIntent}
     * @param newSku the SKU that the user is upgrading or downgrading to
     * @param type of the item being purchased, currently must be "subs"
     * @param developerPayload optional argument to be sent back with the purchase information
     * @return Bundle containing the following key-value pairs
     *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, appropriate response codes
     *                         on failures.
     *         "BUY_INTENT" - PendingIntent to start the purchase flow
     *
     * The Pending intent should be launched with startIntentSenderForResult. When purchase flow
     * has completed, the onActivityResult() will give a resultCode of OK or CANCELED.
     * If the purchase is successful, the result data will contain the following key-value pairs
     *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, appropriate response
     *                         codes on failures.
     *         "INAPP_PURCHASE_DATA" - String in JSON format similar to
     *                                 '{"orderId":"12999763169054705758.1371079406387615",
     *                                   "packageName":"com.example.app",
     *                                   "productId":"exampleSku",
     *                                   "purchaseTime":1345678900000,
     *                                   "purchaseToken" : "122333444455555",
     *                                   "developerPayload":"example developer payload" }'
     *         "INAPP_DATA_SIGNATURE" - String containing the signature of the purchase data that
     *                                  was signed with the private key of the developer
     *                                  TODO: change this to app-specific keys.
     */
    Bundle getBuyIntentToReplaceSkus(int apiVersion, String packageName,
        in List<String> oldSkus, String newSku, String type, String developerPayload);
        
        * Mengembalikan tujuan yang tertunda untuk meluncurkan alur pembelian untuk meningkatkan atau menurunkan versi
     * berlangganan SKU yang ada harus disediakan bersamaan dengan SKU yang baru itu
     * Pengguna sedang melakukan upgrade atau downgrade ke.
     * @param apiVersion billing API versi yang aplikasi ini gunakan, harus 5 atau yang lebih baru
     * @param packageName nama paket dari aplikasi pemanggil
     * @param oldSkus SKU (s) bahwa pengguna melakukan upgrade atau downgrade dari,
     * Jika null atau empty metode ini akan berperilaku seperti {@link # getBuyIntent}
     * @param newSku SKU bahwa pengguna melakukan upgrade atau downgrade ke
     * @param jenis barang yang dibeli, saat ini harus "subs"
     * @param developerPayload argumen opsional untuk dikirim kembali dengan informasi pembelian
     * @return Bundle yang berisi pasangan kunci-nilai berikut
     * "RESPONSE_CODE" dengan nilai int, RESULT_OK (0) jika berhasil, kode respon yang sesuai
     * pada kegagalan
     * "BUY_INTENT" - PendingIntent untuk memulai arus pembelian
     *
     * Tujuan Pending harus diluncurkan dengan startIntentSenderForResult. Saat arus beli
     * telah selesai, onActivityResult () akan memberikan resultCode OK atau DIBATALKAN.
     * Jika pembelian berhasil, data hasil akan berisi pasangan kunci-nilai berikut
     * "RESPONSE_CODE" dengan nilai int, RESULT_OK (0) jika sukses, respon yang tepat
     * kode pada kegagalan
     * "INAPP_PURCHASE_DATA" - String dalam format JSON mirip dengan
     * '{"orderId": "12999763169054705758.1371079406387615",
     * "packageName": "com.example.app",
     * "productId": "exampleSku",
     * "purchaseTime": 1345678900000,
     * "purchaseToken": "122333444455555",
     * "developerPayload": "contoh payload pengembang"} '
     * "INAPP_DATA_SIGNATURE" - String berisi tanda tangan dari data pembelian itu
     * ditandatangani dengan kunci privat pengembang
     * TODO: ubah ini ke kunci khusus aplikasi.
     * /
    Bundle getBuyIntentToReplaceSkus (int apiVersion, String packageName,
        di Daftar <String> oldSkus, String newSku, tipe String, pengembang StringPayload);
}
