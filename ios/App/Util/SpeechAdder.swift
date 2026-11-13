import AVFoundation
import Foundation
import Speech

/// 음성추가: listen in Korean until the user stops, return the phrase.
/// "<상품> 추가" → "<상품>" (same clean-up as Android's cleanVoiceItemName).
@MainActor
final class SpeechAdder: ObservableObject {
    @Published var text = ""
    @Published var listening = false
    /// Shown instead of the transcript when listening can't start.
    @Published var problem: String?
    /// Called with what was heard whenever listening ends (button, final result, error, time limit).
    var onFinish: ((String) -> Void)?

    private let recognizer = SFSpeechRecognizer(locale: Locale(identifier: "ko-KR"))
    private let engine = AVAudioEngine()
    private var request: SFSpeechAudioBufferRecognitionRequest?
    private var task: SFSpeechRecognitionTask?

    func start() async {
        let speechOK = await withCheckedContinuation { c in
            SFSpeechRecognizer.requestAuthorization { c.resume(returning: $0 == .authorized) }
        }
        let micOK = await AVAudioApplication.requestRecordPermission()
        guard speechOK, micOK else { problem = "설정에서 마이크·음성 인식을 허용해 주세요"; return }
        guard let recognizer, recognizer.isAvailable else { problem = "지금은 음성 인식을 쓸 수 없어요. 잠시 후 다시 해 주세요"; return }

        problem = nil
        text = ""
        let session = AVAudioSession.sharedInstance()
        try? session.setCategory(.record, mode: .measurement, options: .duckOthers)
        try? session.setActive(true, options: .notifyOthersOnDeactivation)

        let req = SFSpeechAudioBufferRecognitionRequest()
        req.shouldReportPartialResults = true
        request = req
        let input = engine.inputNode
        input.removeTap(onBus: 0)
        input.installTap(onBus: 0, bufferSize: 1024, format: input.outputFormat(forBus: 0)) { buf, _ in
            req.append(buf)
        }
        engine.prepare()
        do { try engine.start() } catch {
            input.removeTap(onBus: 0)
            try? session.setActive(false, options: .notifyOthersOnDeactivation)
            problem = "마이크를 켤 수 없어요"
            return
        }
        listening = true
        let mine = recognizer.recognitionTask(with: req) { [weak self] result, error in
            Task { @MainActor in
                guard let self, self.request === req else { return }  // ignore a previous session's late callback
                if let result { self.text = result.bestTranscription.formattedString }
                if error != nil || (result?.isFinal ?? false) { self.stop() }
            }
        }
        task = mine
    }

    func stop() {
        guard listening else { return }
        engine.stop()
        engine.inputNode.removeTap(onBus: 0)
        request?.endAudio()
        task?.cancel()
        request = nil
        task = nil
        listening = false
        try? AVAudioSession.sharedInstance().setActive(false, options: .notifyOthersOnDeactivation)
        let heard = text
        text = ""
        if !heard.isEmpty { onFinish?(heard) }
    }

    static func clean(_ raw: String) -> String {
        var s = raw.trimmingCharacters(in: .whitespacesAndNewlines.union(.punctuationCharacters))
        for suffix in ["추가해 줘", "추가해줘", "추가해", "추가"] where s.hasSuffix(suffix) {
            s = String(s.dropLast(suffix.count)).trimmingCharacters(in: .whitespaces)
            break
        }
        return s
    }
}
