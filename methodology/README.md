# [Feature Extraction](/Feature%20Extractor/)

The Jupyter Notebook `Feature_Extractor.ipynb` provides the feature extraction pipeline which extracts the following features and stores the output in 

1. **Spectrogram (STFT):** Computes the spectrogram of the SpiroMask audio signals using the librosa library. The spectrogram provides a visual representation of the audio signals in the frequency domain, allowing the examination of respiratory patterns and related characteristics.

2. **Mel Spectrogram:** Generates the mel spectrogram of the SpiroMask audio signals using the librosa library. The mel spectrogram emphasizes the perceptual frequency bands, providing valuable insights into the distribution of energy in the respiratory audio signals.

3. **MFCC (Mel-frequency cepstral coefficients):** Computes the MFCC features of the SpiroMask audio signals using the speechpy library. MFCCs are widely used in speech and respiratory analysis and provide a compact representation of the spectral characteristics of the audio signals.

## Usage

Feature Extractor uses the [audio_samples](/Feature%20Extractor/Data/audio_samples/) and [ncs](/Feature%20Extractor/Data/ncs//) folder to extract the features. The extracted features are stored in [Autoclip_3000_5000_N95_FEATURES](/Feature%20Extractor/Data/Autoclip_3000_5000_N95_FEATURES.npy) and [NCS_FEATURES](/Feature%20Extractor/Data/NCS_FEATURES.npy) respectively.

# [Model training](/ML%20Model%20Training/)

 In this folder, you'll find machine learning models implemented using the Random Forest. These models have been trained and evaluated using the Leave-One-Out Cross-Validation (LOOCV) technique. The LOOCV approach ensures robust evaluation of the models' performance.

 The models were trained and exported in the ONNX format and were run on android.








