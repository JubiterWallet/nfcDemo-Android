#pragma once
#include <token/TrezorCrypto/TrezorCryptoToken.h>
#include <token/ETH/JubiterBaseETHImpl.h>

namespace jub {
namespace token {


class TrezorCryptoETHImpl :
        public TrezorCryptoToken,
virtual public JubiterBaseETHImpl {

public:
    TrezorCryptoETHImpl(const std::string& XPRVorXPUB) :
        TrezorCryptoToken(XPRVorXPUB) {};
    ~TrezorCryptoETHImpl() {};

    //ETH functions
    virtual JUB_RV SelectApplet();
    virtual JUB_RV GetAppletVersion(std::string& version);
    virtual JUB_RV GetAddress(const std::string& path, const JUB_UINT16 tag, std::string& address);
    virtual JUB_RV GetHDNode(const JUB_BYTE format, const std::string& path, std::string& pubkey);
    virtual JUB_RV SignTX(const bool bERC20,
                          const std::vector<JUB_BYTE>& vNonce,
                          const std::vector<JUB_BYTE>& vGasPrice,
                          const std::vector<JUB_BYTE>& vGasLimit,
                          const std::vector<JUB_BYTE>& vTo,
                          const std::vector<JUB_BYTE>& vValue,
                          const std::vector<JUB_BYTE>& vInput,
                          const std::vector<JUB_BYTE>& vPath,
                          const std::vector<JUB_BYTE>& vChainID,
                          std::vector<JUB_BYTE>& vRaw);
    virtual JUB_RV VerifyTX(const std::vector<JUB_BYTE>& vChainID,
                            const std::string& path,
                            const std::vector<JUB_BYTE>& vSigedTrans);
    virtual JUB_RV SetERC20ETHToken(const std::string& tokenName,
                                    const JUB_UINT16 unitDP,
                                    const std::string& contractAddress);
}; // class TrezorCryptoETHImpl end


} // namespace token end
} // namespace jub end
