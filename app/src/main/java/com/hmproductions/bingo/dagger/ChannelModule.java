package com.hmproductions.bingo.dagger;

import android.content.Context;

import com.hmproductions.bingo.utils.Constants;
import com.squareup.okhttp.ConnectionSpec;

import dagger.Module;
import dagger.Provides;
import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;

@Module (includes = ContextModule.class)
public class ChannelModule {

    String hostIPAddress;

    public ChannelModule(String hostIPAddress) {
        this.hostIPAddress = hostIPAddress;
    }

    @Provides
    @BingoApplicationScope
    public ManagedChannel getManagedChannel() {

        return OkHttpChannelBuilder
                    .forAddress(hostIPAddress, Constants.SERVER_PORT)
                    //.sslSocketFactory(getSocketFactory(context))
                    .connectionSpec(ConnectionSpec.MODERN_TLS)
                    .hostnameVerifier((hostname, session) -> true)
                    .build();
    }

    /* private SSLSocketFactory getSocketFactory(Context context) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, KeyManagementException, UnrecoverableKeyException, GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {

        byte[] der = MiscellaneousUtils.SERVER_CERT.getBytes();
        ByteArrayInputStream crtInputStream = new ByteArrayInputStream(der);

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null);

        CertificateFactory cert_factory = CertificateFactory.getInstance("X509");
        Certificate cert = cert_factory.generateCertificate(crtInputStream);
        trustStore.setCertificateEntry("cert", cert);

        TrustManagerFactory trust_manager_factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());trust_manager_factory.init(trustStore);
        TrustManager[] trust_manager = trust_manager_factory.getTrustManagers();

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
        kmf.init(trustStore, null);
        KeyManager[] keyManagers = kmf.getKeyManagers();

        ProviderInstaller.installIfNeeded(context);
        SSLContext tlsContext = SSLContext.getInstance("TLS");
        tlsContext.init(keyManagers, trust_manager, null);

        return tlsContext.getSocketFactory();
    } */
}
