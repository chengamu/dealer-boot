import { useCallback, useEffect, useRef, useState } from "react";

export function useSmartScroll<T>(dependency: T) {
  const ref = useRef<HTMLDivElement | null>(null);
  const [isPinned, setIsPinned] = useState(true);

  const scrollToBottom = useCallback((behavior: ScrollBehavior = "smooth") => {
    const node = ref.current;
    if (!node) return;
    node.scrollTo({ top: node.scrollHeight, behavior });
  }, []);

  useEffect(() => {
    const node = ref.current;
    if (!node) return;
    const onScroll = () => {
      const distance = node.scrollHeight - node.scrollTop - node.clientHeight;
      setIsPinned(distance < 80);
    };
    node.addEventListener("scroll", onScroll, { passive: true });
    onScroll();
    return () => node.removeEventListener("scroll", onScroll);
  }, []);

  useEffect(() => {
    if (!isPinned) return;
    requestAnimationFrame(() => scrollToBottom("smooth"));
  }, [dependency, isPinned, scrollToBottom]);

  return { ref, isPinned, scrollToBottom };
}
